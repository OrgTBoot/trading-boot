package com.mg.trading.boot.domain.subscribers;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class XOrderManager implements XQuteChangeListener {

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

        if (!CollectionUtils.isEmpty(openOrders) || !CollectionUtils.isEmpty(positions)) {
            log.warn("Skipping {} BUY order placement. There are open orders[{}] or positions[{}].", symbol, openOrders.size(), positions.size());
            return;
        }
        place(strategyDef, broker, OrderAction.BUY, quantity);
    }

    private void placeSell(IStrategyDefinition strategyDef, BrokerProvider broker) {
        String symbol = strategyDef.getSymbol();
        AccountProvider account = broker.account();

        List<Position> openPositions = account.getOpenPositions(symbol);
        List<String> symbolsInSell = account.getOpenOrders(symbol).stream()
                .filter(it -> OrderAction.SELL.equals(it.getAction()))
                .map(it -> it.getTicker().getSymbol())
                .collect(Collectors.toList());

        List<Position> positionsToClose = openPositions.stream()
                .filter(it -> !symbolsInSell.contains(it.getTicker().getSymbol()))
                .collect(Collectors.toList());

        positionsToClose.forEach(position -> place(strategyDef, broker, OrderAction.SELL, position.getQuantity()));
    }

    private void place(IStrategyDefinition strategyDef,
                       BrokerProvider broker,
                       OrderAction action,
                       BigDecimal quantity) {
        String symbol = strategyDef.getSymbol();
        Bar endBar = strategyDef.getSeries().getLastBar();

        OrderRequest orderRequest = OrderRequest.builder()
                .symbol(symbol)
                .action(action)
                .orderType(OrderType.LIMIT)
                .lmtPrice(BigDecimal.valueOf(endBar.getClosePrice().doubleValue()))
                .timeInForce(OrderTimeInForce.GTC)
                .quantity(quantity)
                .build();

        broker.account().placeOrder(orderRequest);
        log.info("{} order placed {}. Bar end time {}", action, orderRequest, endBar.getEndTime());

        printStats(broker, symbol);
    }

    private static TradingRecord getTradingRecord(BrokerProvider broker, String symbol) {
        List<Order> filledOrders = broker.account().getFilledOrdersHistory(symbol, 1);
        return ReportGenerator.buildTradingRecord(filledOrders);
    }

    private void printStats(BrokerProvider broker, String symbol) {
        Integer today = 0;
        TradingLog tradingLog = broker.account().getTradingLog(symbol, today);

        ReportGenerator.printTradingRecords(tradingLog);
        ReportGenerator.printTradingSummary(tradingLog);
    }
}
