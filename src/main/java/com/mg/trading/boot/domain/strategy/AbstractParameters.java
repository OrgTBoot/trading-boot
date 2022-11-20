package com.mg.trading.boot.domain.strategy;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public abstract class AbstractParameters implements Parameters {
    private Range quotesRange;
    private Interval quotesInterval;
    private Integer quotesPullFrequencyInSec;
}
