package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.*;
import com.mg.trading.boot.models.npl.TickerSentiment;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface TickerDetailsProvider {

    @Retryable(value = Throwable.class)
    Ticker getTicker(String symbol);

    @Retryable(value = Throwable.class)
    List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval);

    @Retryable(value = Throwable.class)
    List<TickerNewsArticle> getTickerNews(String symbol, Integer daysRange);

    @Retryable(value = Throwable.class)
    TickerSentiment getTickerSentimentByNews(String symbol, Integer daysRange);
}
