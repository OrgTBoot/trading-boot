package com.mg.trading.boot.strategy.ema;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
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
public class EMAParameters extends StrategyParameters {
    private Integer longBarCount;
    private Integer shortBarCount;
    private Integer minutesToMarketClose;
    private BigDecimal bollingerMultiplier;


    public static EMAParameters optimal(String symbol, BigDecimal sharesQty) {
        return EMAParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(Optional.ofNullable(sharesQty).orElse(BigDecimal.ONE))
                .longBarCount(30)
                .shortBarCount(5)
                .totalLossTolerancePercent(10)
                .minutesToMarketClose(30)
                .quotesRange(Range.ONE_DAY)
                .quotesInterval(Interval.ONE_MINUTE)
                .quotesRollingLimit(1000)
                .quotesPullFrequencyInSec(10)
                .bollingerMultiplier(BigDecimal.valueOf(3))
                .build();
    }
}
