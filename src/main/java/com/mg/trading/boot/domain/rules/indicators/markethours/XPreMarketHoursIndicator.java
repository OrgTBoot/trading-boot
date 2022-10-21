package com.mg.trading.boot.domain.rules.indicators.markethours;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

public class XPreMarketHoursIndicator extends CachedIndicator<Boolean> {

    public XPreMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        return XAbstractMarketHoursIndicator.isPremarketHours(bar);
    }

}
