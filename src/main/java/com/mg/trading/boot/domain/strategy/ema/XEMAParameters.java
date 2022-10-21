package com.mg.trading.boot.domain.strategy.ema;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.strategy.IParameters;
import com.mg.trading.boot.domain.strategy.XAbstractParameters;
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
public class XEMAParameters extends XAbstractParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Integer minutesToMarketClose;
    private BigDecimal bollingerMultiplier;


    public static XEMAParameters optimal() {
        return XEMAParameters.builder()
                .longBarCount(30)
                .shortBarCount(5)
                .totalLossThresholdPercent(10)
                .minutesToMarketClose(30)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesPullFrequencyInSec(10)
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .build();
    }
}
