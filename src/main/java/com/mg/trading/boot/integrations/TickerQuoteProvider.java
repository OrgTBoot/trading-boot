package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import com.mg.trading.boot.models.TickerQuote;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface TickerQuoteProvider {

    @Retryable(value = Throwable.class)
    List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval);
}
