package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.data.Interval;
import com.mg.trading.boot.data.TradingPeriod;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public abstract class StrategyParameters {
    private Number stopGainPercent;
    private Number stopLossPercent;
    private TradingPeriod tradingPeriod;
    private Interval quotesInterval;
    private Integer quotesRollingLimit;
    private Integer quotesPullFrequencyInSec;
    private Integer quotesPullLimit;
    private String symbol;
    private BigDecimal sharesQty;
}
