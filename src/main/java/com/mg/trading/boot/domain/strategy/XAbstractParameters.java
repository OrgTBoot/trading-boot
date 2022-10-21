package com.mg.trading.boot.domain.strategy;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public abstract class XAbstractParameters implements IParameters {
    private Number totalLossThresholdPercent;
    private Range quotesRange;
    private Interval quotesInterval;
    private Integer quotesPullFrequencyInSec;
}
