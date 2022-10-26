package com.mg.trading.boot.domain.subscribers;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.mg.trading.boot.domain.models.OrderAction.BUY;
import static com.mg.trading.boot.domain.models.OrderAction.SELL;

@Log4j2
@Service
public class OrderManagementService implements QuteChangeListener {

    @Override
    public void onQuoteChange(IStrategyDefinition strategyDef, BrokerProvider broker) {
        TradingRecord tradingRecord = getTradingRecord(broker, strategyDef.getSymbol());
        int lastBarIdx = strategyDef.getSeries().getEndIndex();

        Strategy strategy = strategyDef.getStrategy();

        if (strategy.shouldEnter(lastBarIdx, tradingRecord)) {
            placeBuy(strategyDef, broker, BigDecimal.ONE); //todo: implement shares calculation

        } else if (strategy.shouldExit(lastBarIdx, tradingRecord)) {
            placeSell(strategyDef, broker);
        }
    }


    private void placeBuy(IStrategyDefinition strategyDef, BrokerProvider broker, BigDecimal quantity) {
        String symbol = strategyDef.getSymbol();
        AccountProvider account = broker.account();

        List<Order> openOrders = account.getOpenOrders(symbol);
        List<Position> positions = account.getOpenPositions(symbol);

        if (notEmpty(openOrders) || notEmpty(positions)) {
            log.warn("Skipping BUY {}. There are open {} orders or {} positions.", symbol, openOrders.size(), positions.size());
            return;
        }
        place(strategyDef, broker, BUY, quantity);
    }

    private void placeSell(IStrategyDefinition strategyDef, BrokerProvider broker) {
        String symbol = strategyDef.getSymbol();
        AccountProvider account = broker.account();
        List<Order> currentOrders = account.getOpenOrders(symbol);

        List<Order> buyOrders = filter(currentOrders, BUY);
        if (notEmpty(buyOrders)) {
            log.warn("SELL {} received but there are {} BUY orders, canceling them...", symbol, buyOrders.size());
            buyOrders.forEach(order -> cancelOrder(account, order));
        }

        List<Order> sellOrders = filter(currentOrders, SELL);
        if (notEmpty(sellOrders)) { //update sell orders with the current market price
            sellOrders.forEach(order -> updateSellOrderPrice(account, strategyDef, order));

        } else { // submit sell orders
            List<Position> positions = account.getOpenPositions(symbol);
            positions.forEach(position -> place(strategyDef, broker, OrderAction.SELL, position.getQuantity()));
        }
    }

    private void updateSellOrderPrice(AccountProvider account, IStrategyDefinition strategyDef, Order order) {
        Bar endBar = strategyDef.getSeries().getLastBar();

        OrderRequest orderRequest = OrderRequest.builder()
                .orderId(order.getId())
                .symbol(order.getTicker().getSymbol())
                .action(order.getAction())
                .orderType(OrderType.LIMIT)
                .timeInForce(OrderTimeInForce.GTC)
                .quantity(order.getTotalQuantity())
                .lmtPrice(BigDecimal.valueOf(endBar.getClosePrice().doubleValue()))
                .build();

        log.info("Updating {} {} order {} -> {}.",
                orderRequest.getAction(),
                orderRequest.getSymbol(),
                order.getLmtPrice(),
                orderRequest.getLmtPrice());

        account.updateOrder(orderRequest);
    }

    private void place(IStrategyDefinition strategyDef, BrokerProvider broker, OrderAction action, BigDecimal quantity) {
        String symbol = strategyDef.getSymbol();
        Bar endBar = strategyDef.getSeries().getLastBar();

        OrderRequest orderRequest = OrderRequest.builder()
                .symbol(symbol).action(action)
                .orderType(OrderType.LIMIT)
                .lmtPrice(BigDecimal.valueOf(endBar.getClosePrice().doubleValue()))
                .timeInForce(OrderTimeInForce.GTC)
                .quantity(quantity)
                .build();

        broker.account().placeOrder(orderRequest);
        log.info("Sending {} {} order at {}. Bar end time {}", action, symbol, orderRequest.getLmtPrice(), endBar.getEndTime());

        printStats(broker, symbol);
    }

    private static TradingRecord getTradingRecord(BrokerProvider broker, String symbol) {
        List<Order> filledOrders = broker.account().getFilledOrdersHistory(symbol, 1);
        return ReportGenerator.buildTradingRecord(filledOrders);
    }

    private void printStats(BrokerProvider broker, String symbol) {
        Integer today = 0;
        TradingLog tradingLog = broker.account().getTradingLog(symbol, today);

        log.warn("This trading records snapshot might not include latest trade.");
        ReportGenerator.printTradingRecords(tradingLog);
        ReportGenerator.printTradingSummary(tradingLog);
    }


    @SneakyThrows
    private void cancelOrder(AccountProvider account, Order order) {
        account.cancelOrder(order.getId());

        for (int i = 0; i <= 60; i++) {
            List<Order> openOrders = account.getOpenOrders(order.getTicker().getSymbol());
            if (!containsOrder(openOrders, order)) {
                log.debug("Order {} {} cancellation confirmed.", order.getAction(), order.getTicker().getSymbol());
                return;
            }
            Thread.sleep(1000);
            log.debug("Waiting for order {} {} cancellation.", order.getAction(), order.getTicker().getSymbol());
        }
        log.error("Failed to cancel {} {} order.", order.getAction(), order.getTicker().getSymbol());
    }


    private boolean notEmpty(List<?> list) {
        return !CollectionUtils.isEmpty(list);
    }

    private List<Order> filter(List<Order> orders, OrderAction action) {
        return orders.stream().filter(it -> action.equals(it.getAction())).collect(Collectors.toList());
    }

    private boolean containsOrder(List<Order> orders, Order order) {
        return orders.stream().anyMatch(it -> it.getId().equalsIgnoreCase(order.getId()));
    }


}
