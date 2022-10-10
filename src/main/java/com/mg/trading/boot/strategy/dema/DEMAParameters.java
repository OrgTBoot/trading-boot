package com.mg.trading.boot.strategy.dema;

import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import com.mg.trading.boot.strategy.core.StrategyParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DEMAParameters extends StrategyParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Number stopGainPercent;

    public static DEMAParameters optimal(String symbol) {
        return DEMAParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(BigDecimal.ONE)
                .stopLossPercent(2)
                .stopGainPercent(3)
                .longBarCount(60)
                .shortBarCount(5)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE) //bar interval, ex: open-close interval of the bar is 1 minute
                .quotesRollingLimit(1000)
                .quotesPullFrequencyInSec(5) // pull refreshed quotes each X seconds
                .build();
    }
}
