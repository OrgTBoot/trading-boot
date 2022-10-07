package com.mg.trading.boot.strategy.goldencross;

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

    public static DEMAParameters optimal(String symbol) {
        return DEMAParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(BigDecimal.ONE)
                .stopLossPercent(1.5)
                .stopGainPercent(3)
                .longBarCount(60)
                .shortBarCount(5)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE) //bar interval, ex: open-close interval of the bar is 1 minute
                .quotesRollingLimit(1000) //keep total of 1k rolling bars.
                .quotesPullRange(Range.FIFE_MINUTES) // limit quotes pull to 5 elements. In other words last 5 minutes, 5
                // bars/quotes
                .quotesPullFrequencyInSec(10) // pull refreshed quotes each 10 seconds
                .build();
    }
}
