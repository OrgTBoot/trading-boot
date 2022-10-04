package com.mg.trading.boot.strategy.indicators;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

import java.time.ZonedDateTime;

@Log4j2
public class AfterMarketHoursIndicator extends CachedIndicator<Boolean> {

    public AfterMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    protected Boolean calculate(int index) {
        return isAfterMarketHours(index);
    }


    /**
     * After marked hours are considered to be between 4:00PM - 8:00PM EST
     *
     * @param index - index of the bar to evaluate
     * @return - true if bar time is in after-market hours timeframe
     */
    private boolean isAfterMarketHours(int index) {
        Bar bar = getBarSeries().getBar(index);
        ZonedDateTime lastBarDateTime = bar.getEndTime();
        int hour = lastBarDateTime.getHour();

        return hour >= 16 && hour < 20;
    }
}
