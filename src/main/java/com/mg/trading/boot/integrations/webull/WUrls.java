package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.domain.models.Range;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class WUrls {
    private static final String quotesBase = "https://quotes-gw.webullfintech.com";
    private static final String paperActBase = "https://act.webullfintech.com";
    private static final String usTrade = "https://ustrade.webullfinance.com";


    //------------Common---------------
    public static String news(Long tickerId, Integer limit) {
        return String.format(
                "%s/api/information/news/tickerNews?tickerId=%s&currentNewsId=0&pageSize=%s", quotesBase, tickerId, limit);
    }

    public static String ticker(String symbol) {
        return String.format("%s/api/search/pc/tickers?keyword=%s&pageIndex=1&pageSize=1", quotesBase, symbol);
    }

    public static String quotes(Long tickerId, String type, String extendedTrading, Integer limit) {
        return String.format(
                "%s/api/quote/charts/query?tickerIds=%s&type=%s&extendTrading=%s&count=%s", quotesBase, tickerId, type, extendedTrading, limit);
    }

    public static String quotesByMinute(String tickerId, Range range) {
        String period = range.unit + range.value;
        return String.format("%s/api/quote/charts/queryMinutes?tickerIds=%s&period=%s&extendTrading=1", quotesBase, tickerId, period);
    }

    //------------Paper Trading---------------
    public static String paperAccount(Long accountId) {
        return String.format("%s/1/acc/%s", paper(), accountId);
    }

    public static String paperPlaceOrder(Long accountId, Long tickerId) {
        return String.format("%s/1/acc/%s/orderop/place/%s", paper(), accountId, tickerId);
    }

    //todo - review
    public static String paperCancelOrder(Long accountId, String tickerId) {
        return String.format("%s/1/acc/%s/orderop/cancel/%s", paper(), accountId, tickerId);
    }

    public static String paperFilledOrders(Long accountId, ZonedDateTime startTime) {
        String dateParam = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(startTime);

        return String.format(
                "%s/1/acc/%s/order?dateType=FILLED&pageSize=1000&startTime=%s&lastCreateTime0=0&status=Filled",
                paper(), accountId, dateParam);
    }

    //------------Real Trading---------------
    public static String openOrders(Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/openOrders?secAccountId=%s", usTrade, accountId);
    }

    public static String orders(Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/list?secAccountId=%s", usTrade, accountId);
    }

    public static String accountSummary(Long accountId) {
        return String.format("%s/api/trading/v1/webull/account/accountAssetSummary/v2?secAccountId=%s", usTrade, accountId);
    }

    public static String placeOrder(Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/stockOrderPlace?secAccountId=%s", usTrade, accountId);
    }

    public static String cancelOrder(String orderId, String serialId, Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/stockOrderCancel?orderId=%s&serialId=%s&secAccountId=%s",
                usTrade, orderId, serialId, accountId);
    }

    public static String placeOrderCombo(Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/comboOrderPlace?secAccountId=%s", usTrade, accountId);
    }

    public static String modifyOrderCombo(Long accountId) {
        return String.format("%s/api/trading/v1/webull/order/comboOrderModify?secAccountId=%s", usTrade, accountId);
    }

    private static String paper() {
        return String.format("%s/webull-paper-center/api/paper", paperActBase);
    }
}
