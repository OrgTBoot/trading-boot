package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.models.*;
import com.mg.trading.boot.strategy.reporting.TradingReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
public class StrategyOrderExecutor {
    private BrokerProvider brokerProvider;
    private BarSeries series;
    private String symbol;
    private TradingReportGenerator reporting;


    public StrategyOrderExecutor(final TradingReportGenerator reporting,
                                 final BrokerProvider brokerProvider,
                                 final BarSeries series,
                                 final String symbol) {
        this.reporting = reporting;
        this.brokerProvider = brokerProvider;
        this.series = series;
        this.symbol = symbol;
    }

    public void placeBuy(BigDecimal quantity) {
        List<Order> openOrders = this.brokerProvider.getOpenOrders(symbol);
        List<Position> positions = this.brokerProvider.getOpenPositions(symbol);

        if (!CollectionUtils.isEmpty(openOrders) || !CollectionUtils.isEmpty(positions)) {
            log.warn("Skipping BUY order placement. There are open orders[{}] or positions[{}].", openOrders.size(), positions.size());
            return;
        }
        place(OrderAction.BUY, quantity);
    }

    public void placeSell() {
        log.info("Placing SELL order...");
        cancelActiveOrders();

        List<Position> openPositions = this.brokerProvider.getOpenPositions(symbol);
        openPositions.forEach(position -> {
            place(OrderAction.SELL, position.getQuantity());
        });
    }

    private void place(OrderAction action, BigDecimal quantity) {
        int endIndex = series.getEndIndex();
        Bar endBar = series.getBar(endIndex);
        OrderRequest orderRequest = OrderRequest.builder()
                .symbol(symbol)
                .action(action)
                .orderType(OrderType.LIMIT)
                .lmtPrice(BigDecimal.valueOf(endBar.getClosePrice().doubleValue()))
                .timeInForce(OrderTimeInForce.GTC)
                .quantity(quantity)
                .build();

        this.brokerProvider.placeOrder(orderRequest);
        log.info("{} order placed {}. Bar end time {}", action, orderRequest, endBar.getEndTime());

//        DecimalNum price = toDecimalNum(orderRequest.getLmtPrice());
//        DecimalNum qty = toDecimalNum(orderRequest.getQuantity());
//        tradingRecord.operate(endIndex, price, qty);
//
        printStats();
    }


    private void cancelActiveOrders() {
        List<Order> openOrders = this.brokerProvider.getOpenOrders(symbol);
        openOrders.forEach(order -> {
            this.brokerProvider.cancelOrder(order.getId());
            log.warn("Canceling active {} order for symbol {}. ID={}, BarEndTime={}",
                    order.getAction(), symbol, order.getId(), series.getLastBar().getEndTime());
        });
    }

    private void printStats() {
        Integer today = 1;
        TradingRecord tradingRecord = brokerProvider.getTickerTradingRecord(symbol, today);
        reporting.printTradingRecords(tradingRecord);
        reporting.printTradingSummary(tradingRecord, series);
    }

}
