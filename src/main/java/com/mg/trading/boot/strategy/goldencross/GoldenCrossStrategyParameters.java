package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GoldenCrossStrategyParameters extends StrategyParameters {
    private Integer longEMABarCount;
    private Integer shortEMABarCount;


}
