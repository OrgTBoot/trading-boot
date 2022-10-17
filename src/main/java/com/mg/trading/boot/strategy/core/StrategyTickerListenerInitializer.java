package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.reporting.TradingReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class StrategyTickerListenerInitializer {

    public static TickerQuoteExtractor init(BrokerProvider broker,
                                            StrategyParameters params,
                                            Strategy strategy,
                                            BarSeries series) {
        Supplier<List<TickerQuote>> quoteSupplier = () -> broker.ticker().getTickerQuotes(
                params.getSymbol(),
                params.getQuotesRange(),
                params.getQuotesInterval());

        Supplier<Void> onChangeDecisionSupplier = () -> onTickerChange(broker, params, strategy, series);

        TickerQuoteExtractor task = new TickerQuoteExtractor(quoteSupplier, onChangeDecisionSupplier, series);
        log.info("\tQuotes pull task initialized  range={}, interval={} - OK",
                params.getQuotesRange(),
                params.getQuotesInterval());
        return task;
    }

    private static Void onTickerChange(BrokerProvider brokerProvider, StrategyParameters params, Strategy strategy, BarSeries series) {
        TradingReportGenerator reporting = new TradingReportGenerator(params.getSymbol(), strategy, series);
        StrategyOrderExecutor orderExecutor = new StrategyOrderExecutor(reporting, brokerProvider, series, params.getSymbol());

        int lastBarIdx = series.getEndIndex();
        boolean shouldEnter = strategy.shouldEnter(lastBarIdx);
        boolean shouldExit = strategy.shouldExit(lastBarIdx);

        log.debug("Should enter={} | Should exit={}", shouldEnter, shouldExit);
        if (shouldEnter) {
            orderExecutor.placeBuy(params.getSharesQty());

        } else if (shouldExit) {
            orderExecutor.placeSell();
        }
        return null;
    }
}
