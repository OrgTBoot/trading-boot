package com.mg.trading.boot.domain.rules.indicators.markethours;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

/**
 * Rule is satisfied if bar end time is in after-market time frame.
 */
public class XExtendedMarketHoursIndicator extends CachedIndicator<Boolean> {

    public XExtendedMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        boolean isExtendedMktHours = XAbstractMarketHoursIndicator.isExtendedMarketHours(bar);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isExtendedMktHours);

        return isExtendedMktHours;
    }
}
