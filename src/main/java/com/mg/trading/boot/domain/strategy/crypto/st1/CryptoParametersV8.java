package com.mg.trading.boot.domain.strategy.crypto.st1;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.strategy.AbstractParameters;
import com.mg.trading.boot.domain.strategy.dema8.DEMAParametersV8;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CryptoParametersV8 extends AbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private BigDecimal bollingerMultiplier;
    private Integer bollingerBarCount;
    private Integer chandelierBarCount;
    private BigDecimal totalLossThresholdPercent;

    public static CryptoParametersV8 optimal() {
        return CryptoParametersV8.builder()
                .totalLossThresholdPercent(BigDecimal.TEN)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(5)
                .longBarCount(60)
                .shortBarCount(10)
                .bollingerBarCount(60)
                .bollingerMultiplier(BigDecimal.valueOf(3.5))
                .chandelierBarCount(3)
                .build();
    }
}
