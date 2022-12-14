package com.mg.trading.boot.domain.strategy;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;

public interface Parameters {
    /**
     * Quotes range to extract. Ex 1 day range.
     */
    Range getQuotesRange();

    /**
     * Quote interval. Ex 1 minute. To be used in combination with range. Ex: 1 day range of quotes with interval of
     * 1 minute
     */
    Interval getQuotesInterval();

    Integer getQuotesPullFrequencyInSec();
}
