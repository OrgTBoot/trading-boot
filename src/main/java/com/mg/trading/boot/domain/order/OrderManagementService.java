package com.mg.trading.boot.domain.order;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.utils.NumberUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.mg.trading.boot.domain.models.OrderAction.BUY;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Log4j2
@Service
@Scope(value = SCOPE_PROTOTYPE)
public class OrderManagementService implements QuoteChangeListener {

    private final OrderSizingService orderSizingService;

    public OrderManagementService(final OrderSizingService orderSizingService) {
        this.orderSizingService = orderSizingService;
    }

    @Override
    public synchronized void onQuoteChange(StrategyDefinition strategyDef, BrokerProvider broker) {
        TradingRecord tradingRecord = getTradingRecord(broker, strategyDef.getSymbol());
        int lastBarIdx = strategyDef.getSeries().getEndIndex();

        Strategy strategy = strategyDef.getStrategy();
        if (strategy.shouldEnter(lastBarIdx, tradingRecord)) {
            BigDecimal shares = calculateOrderSizeInShares(strategyDef, lastBarIdx);

            placeBuy(strategyDef, broker, shares);

        } else if (strategy.shouldExit(lastBarIdx, tradingRecord)) {
            placeSell(strategyDef, broker);
        }
    }

    private void placeBuy(StrategyDefinition strategyDef, BrokerProvider broker, BigDecimal quantity) {
        String symbol = strategyDef.getSymbol();
        AccountProvider account = broker.account();

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Skipping BUY {}. Calculated position size should be >= 1. Received {}. "
                    + "Not enough money in account?!", symbol, quantity);
            return;
        }

        List<Order> openOrders = account.getOpenOrders(symbol);
        List<Position> positions = account.getOpenPositions(symbol);

        if (notEmpty(openOrders) || notEmpty(positions)) {
            log.debug("Skipping BUY {} of {} shares. There are open {} orders or {} positions.",
                    symbol, quantity, openOrders.size(), positions.size());
            return;
        }
        place(strategyDef, broker, BUY, quantity);
    }

    private void placeSell(StrategyDefinition strategyDef, BrokerProvider broker) {
        String symbol = strategyDef.getSymbol();
        AccountProvider account = broker.account();
        List<Order> openOrders = account.getOpenOrders(symbol);

        List<Order> openBuyOrders = filter(openOrders, BUY);
        if (notEmpty(openBuyOrders)) {
            log.warn("Received {} SELL signal while there are {} open orders. Canceling buy orders...", symbol, openBuyOrders.size());
            openBuyOrders.forEach(it -> account.cancelOrder(it.getId()));

            openOrders = account.getOpenOrders(symbol);
        }

        if (notEmpty(openOrders)) {
            log.warn("Skipping {} SELL. There are {} open orders.", symbol, openOrders.size());
            return;
        }

        List<Position> positions = account.getOpenPositions(symbol);
        positions.forEach(position -> place(strategyDef, broker, OrderAction.SELL, position.getQuantity()));
    }

    private void place(StrategyDefinition strategyDef, BrokerProvider broker, OrderAction action, BigDecimal quantity) {
        String symbol = strategyDef.getSymbol();
        BigDecimal price = this.orderSizingService.getCalculateMarketPrice(symbol, action);

        OrderRequest orderRequest = OrderRequest.builder()
                .symbol(symbol)
                .action(action)
                .orderType(OrderType.LIMIT)
                .lmtPrice(price)
                .timeInForce(OrderTimeInForce.GTC)
                .quantity(quantity)
                .build();

        broker.account().placeOrder(orderRequest);
        log.info("Sending {} {} order of {} shares at {}", action, symbol, quantity, orderRequest.getLmtPrice());

        printStats(broker, symbol);
    }

    private static TradingRecord getTradingRecord(BrokerProvider broker, String symbol) {
        List<Order> filledOrders = broker.account().getFilledOrdersHistory(symbol, 0);
        Position position = broker.account().getOpenPositions(symbol).stream().findFirst().orElse(null);
        log.debug("Trading record: symbol={}, filledOrders={}, position={}", symbol, filledOrders.size(), position);

        return ReportGenerator.buildTradingRecord(filledOrders, position);
    }

    private void printStats(BrokerProvider broker, String symbol) {
        Integer today = 0;
        TradingLog tradingLog = broker.account().getTradingLog(symbol, today);

        log.warn("This trading records snapshot might not include latest trade.");
        ReportGenerator.printTradingRecords(tradingLog);
        ReportGenerator.printTradingSummary(tradingLog);
    }


    private boolean notEmpty(List<?> list) {
        return !CollectionUtils.isEmpty(list);
    }

    private List<Order> filter(List<Order> orders, OrderAction action) {
        return orders.stream().filter(it -> action.equals(it.getAction())).collect(Collectors.toList());
    }


    private BigDecimal calculateOrderSizeInShares(StrategyDefinition strategyDef, int barIdx) {
        Bar bar = strategyDef.getSeries().getBar(barIdx);
        BigDecimal sharePrice = NumberUtils.toRndBigDecimal(bar.getClosePrice());

        return orderSizingService.getOrderSizeInShares(sharePrice);
    }

}
