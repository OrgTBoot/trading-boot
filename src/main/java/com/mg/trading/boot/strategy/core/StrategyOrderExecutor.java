package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.models.*;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.utils.TradingRecordUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
public class StrategyOrderExecutor {
    private BrokerProvider brokerProvider;
    private TradingRecord tradingRecord;
    private BarSeries series;
    private String symbol;


    public StrategyOrderExecutor(final BrokerProvider brokerProvider,
                                 final TradingRecord tradingRecord,
                                 final BarSeries series,
                                 final String symbol) {
        this.brokerProvider = brokerProvider;
        this.tradingRecord = tradingRecord;
        this.series = series;
        this.symbol = symbol;
    }

    private StrategyOrderExecutor() {
    }


    public void place(OrderAction action, BigDecimal quantity) {
        if (action.equals(OrderAction.BUY)) {
            List<Order> openOrders = this.brokerProvider.getOpenOrdersBySymbol(symbol);
            List<Position> positions = this.brokerProvider.getPositionsBySymbol(symbol);

            if (!CollectionUtils.isEmpty(openOrders) || !CollectionUtils.isEmpty(positions)) {
                log.warn("Skipping order placement. There are open orders[{}] or positions[{}].", openOrders.size(), positions.size());
                return;
            }
        }

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

        if (OrderAction.BUY.equals(action)) {
            //TODO: endIdx is not a valid value for trading record - it should be it's own idx schema
            tradingRecord.enter(endIndex, DecimalNum.valueOf(orderRequest.getLmtPrice()), DecimalNum.valueOf(quantity));
        } else if (OrderAction.SELL.equals(action)) {
            tradingRecord.exit(endIndex, DecimalNum.valueOf(orderRequest.getLmtPrice()), DecimalNum.valueOf(quantity));
        } else {
            throw new RuntimeException("Unexpected action received. This should never happen! " + action);
        }

        printStats();
    }

    private void printStats() {
        TradingRecordUtils.printTradingRecords(symbol, tradingRecord);
        TradingRecordUtils.printTradingMetrics(TradingRecordUtils.buildTradingMetrics(symbol, series, tradingRecord));
    }
}
