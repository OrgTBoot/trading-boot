//package com.mg.trading.boot.strategy.core;
//
//import com.mg.trading.boot.integrations.BrokerProvider;
//import com.mg.trading.boot.models.Order;
//import com.mg.trading.boot.models.OrderAction;
//import com.mg.trading.boot.models.Position;
//import com.mg.trading.boot.strategy.TradingStrategyExecutor;
//import lombok.extern.log4j.Log4j2;
//import org.ta4j.core.BaseTradingRecord;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.num.DecimalNum;
//
//import java.util.List;
//import java.util.function.Function;
//
//import static com.google.common.base.Preconditions.checkState;
//
//@Log4j2
//public class TickerTradingRecordExtractor {
//
//    private final BrokerProvider brokerProvider;
//
//    public TradingStrategyExecutor(BrokerProvider brokerProvider) {
//        this.brokerProvider = brokerProvider;
//    }
//
//    public TradingRecord getTradingRecord(String symbol) {
//        TradingRecord record = new BaseTradingRecord();
//        loadSymbolOpenPositions(brokerProvider, record, symbol);
//        loadSymbolOpenOrders(brokerProvider, record, symbol);
//
//        return record;
//    }
//
//    private static void loadSymbolOpenPositions(BrokerProvider broker, TradingRecord record, String symbol) {
//        List<Position> openPositions = broker.getOpenPositions(symbol);
//        checkState(openPositions.size() <= 1, "There should only one position for a given symbol.");
//
//        openPositions.forEach(position -> {
//            int idx = calculateTradeIndex(record);
//            DecimalNum price = DecimalNum.valueOf(position.getCostPrice());
//            DecimalNum qty = DecimalNum.valueOf(position.getQuantity());
//            record.enter(idx, price, qty);
//            log.debug("\t\tAdded historical position to the trading record: {}", position);
//        });
//    }
//
//    private static void loadSymbolOpenOrders(BrokerProvider broker, TradingRecord record, String symbol) {
//        List<Order> openOrders = broker.getOpenOrders(symbol);
//        openOrders.forEach(order -> {
//            int idx = calculateTradeIndex(record);
//            DecimalNum price = DecimalNum.valueOf(order.getLmtPrice());
//            DecimalNum qty = DecimalNum.valueOf(order.getTotalQuantity());
//
//            if (OrderAction.BUY.equals(order.getAction())) {
//                record.enter(idx, price, qty);
//            } else {
//                record.exit(idx, price, qty);
//            }
//            log.debug("\t\tAdded historical {} order to the trading record: {}", order.getAction(), order);
//        });
//    }
//
//    private static int calculateTradeIndex(TradingRecord record) {
//        Function<TradingRecord, Integer> idxFunction = (r) -> r.getLastTrade() != null ? r.getLastTrade().getIndex() + 1 : 0;
//        return idxFunction.apply(record);
//    }
//}
