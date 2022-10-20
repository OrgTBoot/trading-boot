package com.mg.trading.boot.strategy.dema.v2;

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
public class DEMAParametersV2 extends StrategyParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private BigDecimal bollingerMultiplier;
    private Integer chandelierBarCount;
    private Integer minutesToMarketClose;

    public static DEMAParametersV2 optimal(String symbol, BigDecimal sharesQty) {
        return DEMAParametersV2.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(Optional.ofNullable(sharesQty).orElse(BigDecimal.ONE))
                .totalLossTolerancePercent(10)
                .minutesToMarketClose(30)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesRollingLimit(1000)
                .quotesPullFrequencyInSec(5)
                .longBarCount(60)
                .shortBarCount(10)
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .chandelierBarCount(5)
                .build();
    }
}
