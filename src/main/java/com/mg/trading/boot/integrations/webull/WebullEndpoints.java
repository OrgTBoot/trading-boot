package com.mg.trading.boot.integrations.webull;

public enum WebullEndpoints {
    QUOTES("https://quotes-gw.webullfintech.com"),
    TICKER_NEWS(QUOTES.value + "/api/information/news/tickerNews?tickerId=%s&currentNewsId=0&pageSize=%s"),
    TICKER_BY_NAME(QUOTES.value + "/api/search/pc/tickers?keyword=%s&pageIndex=1&pageSize=1"),
    TICKER_QUOTES(QUOTES.value + "/api/quote/charts/query?tickerIds=%s&type=%s&extendTrading=%s&count=%s"),
    TICKER_MINUTE_QUOTES(QUOTES.value + "/api/quote/charts/queryMinutes?tickerIds=%s&period=%s"),

    ACCOUNT("https://act.webullfintech.com"),
    PAPER_ACCOUNT(ACCOUNT.value + "/webull-paper-center/api/paper/1/acc/%s"),
    PAPER_PLACE_ORDER(ACCOUNT.value + "/webull-paper-center/api/paper/1/acc/%s/orderop/place/%s"),
    PAPER_CANCEL_ORDER(ACCOUNT.value + "/webull-paper-center/api/paper/1/acc/%s/orderop/cancel/%s");

    public final String value;

    WebullEndpoints(String value) {
        this.value = value;
    }
}
