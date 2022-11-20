package com.mg.trading.boot.domain.strategy.dema9;

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
public class DEMAParametersV9 extends AbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private BigDecimal bollingerMultiplier;
    private BigDecimal totalLossThresholdPercent;
    private Integer bollingerBarCount;
    private Integer cndBarCount;
    private Double cndMultiplier;

    public static DEMAParametersV9 optimal() {
        return DEMAParametersV9.builder()
                .totalLossThresholdPercent(BigDecimal.TEN)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(5)
                .longBarCount(60)
                .shortBarCount(10)
                .bollingerBarCount(60)
                .bollingerMultiplier(BigDecimal.valueOf(3.5))
                .cndBarCount(3)
                .cndMultiplier(3D)
                .build();
    }
}
