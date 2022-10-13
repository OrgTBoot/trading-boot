package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public abstract class StrategyParameters {

    /**
     * Total loss percentage strategy should tolerate.
     * Use for scenarios when you want to prevent entry in to a stock that performed poor for previous entries
     * and reached X% loss.
     */
    private Number totalLossTolerancePercent;

    private Number positionStopLossPercent;
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
