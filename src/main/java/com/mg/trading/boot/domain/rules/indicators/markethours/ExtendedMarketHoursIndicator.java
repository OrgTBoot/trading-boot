package com.mg.trading.boot.domain.rules.indicators.markethours;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

/**
 * Rule is satisfied if bar end time is in after-market time frame.
 */
public class ExtendedMarketHoursIndicator extends CachedIndicator<Boolean> {

    public ExtendedMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        boolean isExtendedMktHours = AbstractMarketHoursIndicator.isExtendedMarketHours(bar);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isExtendedMktHours);

        return isExtendedMktHours;
    }
}
