package com.mg.trading.boot.strategy.indicators;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

import static com.mg.trading.boot.strategy.indicators.AbstractMarketHoursIndicator.isExtendedMarketHours;

/**
 * Rule is satisfied if bar end time is in after-market time frame.
 */
public class ExtendedMarketHoursIndicator extends CachedIndicator<Boolean> {

    public ExtendedMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    protected Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        return isExtendedMarketHours(bar);
    }
}
