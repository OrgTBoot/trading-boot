package com.mg.trading.boot.domain.strategy.ema;

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
public class EMAParameters extends AbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Integer minutesToMarketClose;
    private BigDecimal bollingerMultiplier;


    public static EMAParameters optimal() {
        return EMAParameters.builder()
                .longBarCount(30)
                .shortBarCount(5)
                .totalLossThresholdPercent(BigDecimal.valueOf(-10))
                .minutesToMarketClose(30)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(10)
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .build();
    }
}
