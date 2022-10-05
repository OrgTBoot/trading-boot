package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.TradingPeriod;
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
                .stopLossPercent(2)
                .stopGainPercent(3)
                .longBarCount(60)
                .shortBarCount(5)
                .tradingPeriod(TradingPeriod.EXTENDED)
                .quotesInterval(Interval.ONE_MINUTE) //bar interval, ex: open-close interval of the bar is 1 minute
                .quotesRollingLimit(360) //keep total of 360 rolling bars, 360=6hours.
                .quotesPullLimit(5) // limit quotes pull to 5 elements. In other words last 5 minutes, 5 bars/quotes
                .quotesPullFrequencyInSec(10) // pull refreshed quotes each 10 seconds
                .build();
    }
}
