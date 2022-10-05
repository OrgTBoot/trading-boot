package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.models.OrderAction;
import com.mg.trading.boot.models.TickerQuote;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.function.Supplier;

import static com.mg.trading.boot.integrations.utils.Mapper.toBigDecimal;

@Log4j2
public class StrategyTickerListenerInitializer {

    public static TickerQuoteExtractor init(BrokerProvider brokerProvider,
                                            StrategyParameters params,
                                            Strategy strategy,
                                            BarSeries series,
                                            TradingRecord tradingRecord) {
        Supplier<List<TickerQuote>> quoteSupplier = () -> brokerProvider.getTickerQuotes(
                params.getSymbol(),
                params.getQuotesInterval(),
                params.getTradingPeriod(),
                params.getQuotesPullLimit());

        Supplier<Void> onChangeDecisionSupplier = () ->
                onTickerChange(brokerProvider, params, strategy, series, tradingRecord);

        TickerQuoteExtractor task = new TickerQuoteExtractor(quoteSupplier, onChangeDecisionSupplier, series);
        log.info("\tQuotes pull task initialized - OK");
        return task;
    }

    private static Void onTickerChange(BrokerProvider brokerProvider,
                                       StrategyParameters params,
                                       Strategy strategy,
                                       BarSeries series,
                                       TradingRecord tradingRecord) {
        StrategyOrderExecutor orderExecutor
                = new StrategyOrderExecutor(brokerProvider, tradingRecord, series, params.getSymbol());

        if (strategy.shouldEnter(series.getEndIndex())) {
            orderExecutor.place(OrderAction.BUY, params.getSharesQty());

        } else if (strategy.shouldExit(series.getEndIndex(), tradingRecord)) {
            Num sharesQty = tradingRecord.getLastEntry().getAmount();
            orderExecutor.place(OrderAction.SELL, toBigDecimal(sharesQty));
        }
        return null;
    }
}
