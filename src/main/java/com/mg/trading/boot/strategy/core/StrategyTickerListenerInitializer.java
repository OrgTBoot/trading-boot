package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.models.Order;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.reporting.ReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class StrategyTickerListenerInitializer {

    public static QuoteListener init(BrokerProvider broker,
                                     StrategyParameters params,
                                     Strategy strategy,
                                     BarSeries series) {
        Supplier<List<TickerQuote>> quoteSupplier = () -> broker.ticker().getTickerQuotes(
                params.getSymbol(),
                params.getQuotesRange(),
                params.getQuotesInterval());

        Supplier<Void> onChangeDecisionSupplier = () -> onTickerChange(broker, params, strategy, series);

        QuoteListener task = new QuoteListener(quoteSupplier, onChangeDecisionSupplier, series);
        log.info("\tQuotes pull task initialized  range={}, interval={} - OK",
                params.getQuotesRange(),
                params.getQuotesInterval());
        return task;
    }

    private static Void onTickerChange(BrokerProvider broker, StrategyParameters params, Strategy strategy, BarSeries series) {
        StrategyOrderExecutor orderExecutor = new StrategyOrderExecutor(broker, series, params.getSymbol());


        TradingRecord tradingRecord = getTradingRecord(broker, params.getSymbol());
        int lastBarIdx = series.getEndIndex();

        if (strategy.shouldEnter(lastBarIdx, tradingRecord)) {
            orderExecutor.placeBuy(params.getSharesQty());

        } else if (strategy.shouldExit(lastBarIdx, tradingRecord)) {
            orderExecutor.placeSell();
        }
        return null;
    }

    private static TradingRecord getTradingRecord(BrokerProvider broker, String symbol) {
        List<Order> filledOrders = broker.account().getFilledOrdersHistory(symbol, 1);
        return ReportGenerator.buildTradingRecord(filledOrders);
    }
}
