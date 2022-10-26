package com.mg.trading.boot.integrations;

import com.mg.trading.boot.domain.models.*;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface AccountProvider {


    @Retryable(value = Throwable.class)
    Account getAccount();

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders();

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders(String symbol);

    @Retryable(value = Throwable.class)
    List<Order> getFilledOrdersHistory(String symbol, Integer daysRange);

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions(String symbol);

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions();

    @Retryable(value = Throwable.class)
    TradingLog getTradingLog(String symbol, Integer daysRange);

    @Retryable(value = Throwable.class)
    List<TradingLog> getTradingLogs(Integer daysRange);

    void placeOrder(OrderRequest orderRequest);

    void updateOrder(OrderRequest order);

    void cancelOrder(String id);

}
