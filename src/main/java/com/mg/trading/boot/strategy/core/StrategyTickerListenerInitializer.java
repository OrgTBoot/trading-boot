package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.reporting.TradingReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class StrategyTickerListenerInitializer {

    public static TickerQuoteExtractor init(TickerQuoteProvider quoteProvider,
                                            BrokerProvider brokerProvider,
                                            StrategyParameters params,
                                            Strategy strategy,
                                            BarSeries series) {
        Supplier<List<TickerQuote>> quoteSupplier = () -> quoteProvider.getTickerQuotes(
                params.getSymbol(),
                params.getQuotesRange(),
                params.getQuotesInterval());

        Supplier<Void> onChangeDecisionSupplier = () -> onTickerChange(brokerProvider, params, strategy, series);

        TickerQuoteExtractor task = new TickerQuoteExtractor(quoteSupplier, onChangeDecisionSupplier, series);
        log.info("\tQuotes pull task initialized  range={}, interval={} - OK",
                params.getQuotesRange(),
                params.getQuotesInterval());
        return task;
    }

    private static Void onTickerChange(BrokerProvider brokerProvider, StrategyParameters params, Strategy strategy, BarSeries series) {
        TradingReportGenerator reporting = new TradingReportGenerator(params.getSymbol(), strategy);
        StrategyOrderExecutor orderExecutor = new StrategyOrderExecutor(reporting, brokerProvider, series, params.getSymbol());

        int lastBarIdx = series.getEndIndex();
        if (strategy.shouldEnter(lastBarIdx)) {
            orderExecutor.placeBuy(params.getSharesQty());

        } else if (strategy.shouldExit(lastBarIdx)) {
            orderExecutor.placeSell();
        }
        return null;
    }
}
