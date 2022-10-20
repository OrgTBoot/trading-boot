package com.mg.trading.boot.strategy.indicators;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

import static com.mg.trading.boot.strategy.indicators.AbstractMarketHoursIndicator.isPremarketHours;

public class PreMarketHoursIndicator extends CachedIndicator<Boolean> {

    public PreMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    protected Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        return isPremarketHours(bar);
    }

}
