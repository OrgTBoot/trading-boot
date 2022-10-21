package com.mg.trading.boot.domain.rules.indicators.markethours;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;


public class XMarketHoursIndicator extends CachedIndicator<Boolean> {

    public XMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        return XAbstractMarketHoursIndicator.isMarketHours(bar);
    }

}
