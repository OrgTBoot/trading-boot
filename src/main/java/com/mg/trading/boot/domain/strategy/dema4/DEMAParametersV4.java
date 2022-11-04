package com.mg.trading.boot.domain.strategy.dema4;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.strategy.AbstractParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DEMAParametersV4 extends AbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private BigDecimal bollingerMultiplier;
    private Integer chandelierBarCount;

    public static DEMAParametersV4 optimal() {
        return DEMAParametersV4.builder()
                .totalLossThresholdPercent(BigDecimal.TEN)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(5)
                .longBarCount(60)
                .shortBarCount(10)
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .chandelierBarCount(5)
                .build();
    }
}