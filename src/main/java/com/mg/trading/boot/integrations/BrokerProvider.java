package com.mg.trading.boot.integrations;

import com.mg.trading.boot.data.*;
import com.mg.trading.boot.data.npl.TickerSentiment;

import java.util.List;

public interface BrokerProvider {

    Ticker getTicker(String symbol);

    /**
     * Get ticker day quotes
     *
     * @param symbol        - ticker symbol
     * @param interval      - interval of the aggregated information of each quote
     * @param tradingPeriod - day trading period
     * @param limit         - quotes limit
     * @return - a list of ticker quotes
     */
    List<TickerQuote> getTickerQuotes(String symbol,
                                      Interval interval,
                                      TradingPeriod tradingPeriod,
                                      Integer limit);

    List<TickerNewsArticle> getTickerNews(String symbol, Long daysAgoRelevance);

    TickerSentiment getTickerSentimentByNews(String symbol, Long daysAgoRelevance);

    void placeOrder(OrderRequest orderRequest);

    void cancelOrder(String orderId);

//    Order getOrderById(String id);

    List<Order> getOpenOrders();

    List<Position> getPositions();

    List<Position> getPositionsBySymbol(String symbol);

    List<Order> getOpenOrdersBySymbol(String symbol);

//    Order getOpenOrderById(String id);

    //----------------------REVISIT BELLOW ONES

}
