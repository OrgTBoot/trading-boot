package com.mg.trading.boot.strategy.indicators;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

import java.time.ZonedDateTime;

public class MarketHoursIndicator extends CachedIndicator<Boolean> {

    public MarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    protected Boolean calculate(int index) {
        return isMarketHours(index);
    }

    /**
     * Marked hours are considered to be between 4:00AM - 4:00PM EST
     * Note that premarket hours are included as well: 4:00AM - 9:30AM
     *
     * @param index - index of the bar to evaluate
     * @return - true if bar time is in market hours timeframe
     */
    private boolean isMarketHours(int index) {
        Bar bar = getBarSeries().getBar(index);
        ZonedDateTime lastBarDateTime = bar.getEndTime();
        int hour = lastBarDateTime.getHour();

        return (hour >= 4 && hour < 16);
    }
}
