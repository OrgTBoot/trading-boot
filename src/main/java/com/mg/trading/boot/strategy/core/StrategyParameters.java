package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public abstract class StrategyParameters {
    private BrokerProvider brokerProvider;
    private TickerQuoteProvider quoteProvider;

    private Number stopLossPercent;
    /**
     * Quotes range to extract. Ex 1 day range.
     */
    private Range quotesRange;
    /**
     * Quote interval. Ex 1 minute. To be used in combination with range. Ex: 1 day range of quotes with interval of
     * 1 minute
     */
    private Interval quotesInterval;
    private Integer quotesRollingLimit;
    private Integer quotesPullFrequencyInSec;
    private BigDecimal sharesQty;
    private String symbol;
}
