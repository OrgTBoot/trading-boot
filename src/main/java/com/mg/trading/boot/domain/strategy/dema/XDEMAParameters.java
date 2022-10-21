package com.mg.trading.boot.domain.strategy.dema;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.strategy.XAbstractParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class XDEMAParameters extends XAbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Number positionStopGainPercent;
    private Number positionStopLossPercent;
    private BigDecimal bollingerMultiplier;
    private Integer minutesToMarketClose;


    public static XDEMAParameters optimal() {
        return XDEMAParameters.builder()
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .totalLossThresholdPercent(10)
                .minutesToMarketClose(30)
                .positionStopLossPercent(2)
                .positionStopGainPercent(3)
                .longBarCount(60)
                .shortBarCount(10)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(5)
                .build();
    }
}
