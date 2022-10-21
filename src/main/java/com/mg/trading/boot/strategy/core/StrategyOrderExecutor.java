package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class StrategyOrderExecutor {
    private final BrokerProvider broker;
    private final BarSeries series;
    private final String symbol;

    public StrategyOrderExecutor(final BrokerProvider broker,
                                 final BarSeries series,
                                 final String symbol) {
        this.broker = broker;
        this.series = series;
        this.symbol = symbol;
    }

    public void placeBuy(BigDecimal quantity) {
        List<Order> openOrders = this.broker.account().getOpenOrders(symbol);
        List<Position> positions = this.broker.account().getOpenPositions(symbol);

        if (!CollectionUtils.isEmpty(openOrders) || !CollectionUtils.isEmpty(positions)) {
            log.warn("Skipping {} BUY order placement. There are open orders[{}] or positions[{}].",
                    symbol, openOrders.size(), positions.size());
            return;
        }
        place(OrderAction.BUY, quantity);
    }

    public void placeSell() {
        AccountProvider account = this.broker.account();
        List<Position> openPositions = account.getOpenPositions(symbol);
        List<String> symbolsInSell = account.getOpenOrders(symbol).stream()
                .filter(it -> OrderAction.SELL.equals(it.getAction())).map(it -> it.getTicker().getSymbol())
                .collect(Collectors.toList());

        List<Position> positionsToClose = openPositions.stream()
                .filter(it -> !symbolsInSell.contains(it.getTicker().getSymbol()))
                .collect(Collectors.toList());

        positionsToClose.forEach(position -> place(OrderAction.SELL, position.getQuantity()));
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

        this.broker.account().placeOrder(orderRequest);
        log.info("{} order placed {}. Bar end time {}", action, orderRequest, endBar.getEndTime());

        printStats();
    }


    private void cancelActiveBuyOrders() {
        List<Order> openOrders = this.broker.account().getOpenOrders(symbol);
        openOrders.forEach(order -> {
            this.broker.account().cancelOrder(order.getId());
            log.warn("Canceling active {} order for symbol {}. ID={}, BarEndTime={}",
                    order.getAction(), symbol, order.getId(), series.getLastBar().getEndTime());
        });
    }

    private void printStats() {
        Integer today = 0;
        TradingLog tradingLog = broker.account().getTradingLog(symbol, today);
        ReportGenerator.printTradingRecords(tradingLog);
        ReportGenerator.printTradingSummary(tradingLog);
    }

}
