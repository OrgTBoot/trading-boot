package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.*;
import com.mg.trading.boot.models.npl.TickerSentiment;
import org.springframework.retry.annotation.Retryable;
import org.ta4j.core.TradingRecord;

import java.util.List;

public interface BrokerProvider {

    @Retryable(value = Throwable.class)
    Ticker getTicker(String symbol);

    @Retryable(value = Throwable.class)
    List<TickerNewsArticle> getTickerNews(String symbol, Long daysAgoRelevance);

    @Retryable(value = Throwable.class)
    TickerSentiment getTickerSentimentByNews(String symbol, Long daysAgoRelevance);

    void placeOrder(OrderRequest orderRequest);

    @Retryable(value = Throwable.class)
    void cancelOrder(String orderId);

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders();

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions();

    @Retryable(value = Throwable.class)
    List<Position> getOpenPositions(String symbol);

    @Retryable(value = Throwable.class)
    List<Order> getOpenOrders(String symbol);

    /**
     * Extracts symbol orders.
     *
     * @param symbol    - symbol to extract orders history for
     * @param daysRange - number of days to extract
     * @return - a list of symbol historical orders
     */
    @Retryable(value = Throwable.class)
    List<Order> getOrdersHistory(String symbol, Integer daysRange);


    @Retryable(value = Throwable.class)
    TradingRecord getTickerTradingRecord(String symbol, Integer daysRange);

}
