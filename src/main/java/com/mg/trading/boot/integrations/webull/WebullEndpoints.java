package com.mg.trading.boot.integrations.webull;

/**
 * Broker endpoints.
 * <p>
 * Note: There are endpoints under different domains, to avoid confusion we intentionally stick to the full URLs.
 */
public enum WebullEndpoints {
    TICKER_NEWS("https://quotes-gw.webullfintech.com/api/information/news/tickerNews?tickerId=%s&currentNewsId=0&pageSize=%s"),
    TICKER_BY_NAME("https://quotes-gw.webullfintech.com/api/search/pc/tickers?keyword=%s&pageIndex=1&pageSize=1"),
    TICKER_QUOTES("https://quotes-gw.webullfintech.com/api/quote/charts/query?tickerIds=%s&type=%s&extendTrading=%s&count=%s"),
    PAPER_ACCOUNT("https://act.webullfintech.com/webull-paper-center/api/paper/1/acc/%s"),
    PAPER_PLACE_ORDER("https://act.webullfintech.com/webull-paper-center/api/paper/1/acc/%s/orderop/place/%s"),
    PAPER_CANCEL_ORDER("https://act.webullfintech.com/webull-paper-center/api/paper/1/acc/%s/orderop/cancel/%s");

    public final String value;

    WebullEndpoints(String value) {
        this.value = value;
    }
}
