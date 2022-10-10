package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.*;
import com.mg.trading.boot.models.npl.TickerSentiment;

import java.util.List;

public interface BrokerProvider {

    Ticker getTicker(String symbol);


    List<TickerNewsArticle> getTickerNews(String symbol, Long daysAgoRelevance);

    TickerSentiment getTickerSentimentByNews(String symbol, Long daysAgoRelevance);

    void placeOrder(OrderRequest orderRequest);

    void cancelOrder(String orderId);

    List<Order> getOpenOrders();

    List<Position> getPositions();

    List<Position> getPositionsBySymbol(String symbol);

    List<Order> getOpenOrdersBySymbol(String symbol);

}
