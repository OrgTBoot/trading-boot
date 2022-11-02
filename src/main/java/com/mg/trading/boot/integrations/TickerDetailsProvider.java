package com.mg.trading.boot.integrations;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.models.Ticker;
import com.mg.trading.boot.domain.models.TickerQuote;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface TickerDetailsProvider {

    @Retryable(value = Throwable.class)
    Ticker getTicker(String symbol);

    @Retryable(value = Throwable.class)
    List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval);

    @Retryable(value = Throwable.class)
    TickerQuote getLatestTickerQuote(String symbol);

}
