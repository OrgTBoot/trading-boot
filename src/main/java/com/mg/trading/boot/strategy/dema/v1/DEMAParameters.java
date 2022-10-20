package com.mg.trading.boot.strategy.dema.v1;

import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import com.mg.trading.boot.strategy.core.StrategyParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Optional;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DEMAParameters extends StrategyParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Number positionStopGainPercent;
    private Number positionStopLossPercent;
    private BigDecimal bollingerMultiplier;
    private Integer minutesToMarketClose;


    public static DEMAParameters optimal(String symbol, BigDecimal sharesQty) {
        return DEMAParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(Optional.ofNullable(sharesQty).orElse(BigDecimal.ONE))
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .totalLossTolerancePercent(10)
                .minutesToMarketClose(30)
                .positionStopLossPercent(2)
                .positionStopGainPercent(3)
                .longBarCount(60)
                .shortBarCount(10)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesRollingLimit(1000)
                .quotesPullFrequencyInSec(5)
                .build();
    }
}
