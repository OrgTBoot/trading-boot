package com.mg.trading.boot.strategy.ema;

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
public class EMAParameters extends StrategyParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Number stopGainPercent;


    public static EMAParameters optimal(String symbol) {
        return EMAParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(BigDecimal.ONE)
                .stopLossPercent(2)
                .stopGainPercent(3)
                .longBarCount(30)
                .shortBarCount(5)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE) //bar interval, ex: open-close interval of the bar is 1 minute
                .quotesRollingLimit(1000) //keep total of 1k rolling bars.
                .quotesPullFrequencyInSec(10) // pull refreshed quotes each 10 seconds
                .build();
    }
}