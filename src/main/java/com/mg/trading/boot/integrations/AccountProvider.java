package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.Account;
import com.mg.trading.boot.models.Order;
import com.mg.trading.boot.models.OrderRequest;
import com.mg.trading.boot.models.Position;
import org.springframework.retry.annotation.Retryable;
import org.ta4j.core.TradingRecord;

import java.util.List;

public interface AccountProvider {


    @Retryable(value = Throwable.class)
    Account getAccount();

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders();

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders(String symbol);

    @Retryable(value = Throwable.class)
    List<Order> getOrdersHistory(String symbol, Integer daysRange);

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions(String symbol);

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions();

    @Retryable(value = Throwable.class)
    TradingRecord getTickerTradingRecord(String symbol, Integer daysRange);

    void placeOrder(OrderRequest orderRequest);

    void cancelOrder(String id);

}
