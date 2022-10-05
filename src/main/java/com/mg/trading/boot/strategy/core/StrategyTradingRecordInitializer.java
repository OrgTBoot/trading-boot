package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.models.Order;
import com.mg.trading.boot.models.Position;
import com.mg.trading.boot.integrations.BrokerProvider;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.util.List;
import java.util.function.Function;

@Log4j2
public class StrategyTradingRecordInitializer {

    /**
     * Initialize trading record with existent ticker open positions.
     */
    public static TradingRecord init(BrokerProvider brokerProvider, StrategyParameters params) {
        log.info("\tInitializing trading records...");

        TradingRecord tradingRecord = new BaseTradingRecord();
        List<Position> tickerPositions = brokerProvider.getPositionsBySymbol(params.getSymbol());

        Function<TradingRecord, Integer> geIdx = (tr) -> tr.getLastTrade() != null ? tr.getLastTrade().getIndex() + 1 : 0;

        tickerPositions.forEach(position -> {
            int idx = geIdx.apply(tradingRecord);
            DecimalNum entryPrice = DecimalNum.valueOf(position.getCostPrice());
            DecimalNum qty = DecimalNum.valueOf(position.getQuantity());
            tradingRecord.enter(idx, entryPrice, qty);
            log.debug("\t\tAdded historical position to the trading record: {}", position);
        });

        List<Order> tickerOpenOrders = brokerProvider.getOpenOrdersBySymbol(params.getSymbol());
        tickerOpenOrders.forEach(order -> {
            int idx = geIdx.apply(tradingRecord);
            DecimalNum entryPrice = DecimalNum.valueOf(order.getLmtPrice());
            DecimalNum qty = DecimalNum.valueOf(order.getTotalQuantity());
            tradingRecord.exit(idx, entryPrice, qty);
            log.debug("\t\tAdded historical {} order to the trading record: {}", order.getAction(), order);
        });

        log.info("\tInitialized trading records -OK");
        return tradingRecord;
    }
}
