package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.models.OrderAction;
import com.mg.trading.boot.models.TickerQuote;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class StrategyTickerListenerInitializer {

    public static TickerQuoteExtractor init(TickerQuoteProvider quoteProvider,
                                            BrokerProvider brokerProvider,
                                            StrategyParameters params,
                                            Strategy strategy,
                                            BarSeries series,
                                            TradingRecord tradingRecord) {
        Supplier<List<TickerQuote>> quoteSupplier = () -> quoteProvider.getTickerQuotes(
                params.getSymbol(),
                params.getQuotesPullRange(),
                params.getQuotesInterval());

        Supplier<Void> onChangeDecisionSupplier = () ->
                onTickerChange(brokerProvider, params, strategy, series, tradingRecord);

        TickerQuoteExtractor task = new TickerQuoteExtractor(quoteSupplier, onChangeDecisionSupplier, series);
        log.info("\tQuotes pull task initialized  range={}, interval={} - OK",
                params.getQuotesPullRange(),
                params.getQuotesInterval());
        return task;
    }

    private static Void onTickerChange(BrokerProvider brokerProvider,
                                       StrategyParameters params,
                                       Strategy strategy,
                                       BarSeries series,
                                       TradingRecord tradingRecord) {
        StrategyOrderExecutor orderExecutor
                = new StrategyOrderExecutor(brokerProvider, strategy, tradingRecord, series, params.getSymbol());

        if (strategy.shouldEnter(series.getEndIndex())) {
            orderExecutor.place(OrderAction.BUY, params.getSharesQty());

        } else if (strategy.shouldExit(series.getEndIndex(), tradingRecord)) {
            BigDecimal sharesQty = toRndBigDecimal(tradingRecord.getLastEntry().getAmount());
            orderExecutor.place(OrderAction.SELL, sharesQty);
        }
        return null;
    }
}
