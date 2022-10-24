package com.mg.trading.boot.domain.strategy.dema3;

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
public class DEMAParametersV3 extends AbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private BigDecimal bollingerMultiplier;
    private Integer chandelierBarCount;
    private Integer minutesToMarketClose;

    public static DEMAParametersV3 optimal() {
        return DEMAParametersV3.builder()
                .totalLossThresholdPercent(BigDecimal.valueOf(-10))
                .minutesToMarketClose(30)
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
