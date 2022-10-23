package com.mg.trading.boot.domain.indicators.markethours;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

public class PreMarketHoursIndicator extends CachedIndicator<Boolean> {

    public PreMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        boolean isPremarketHours = AbstractMarketHoursIndicator.isPremarketHours(bar);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isPremarketHours);

        return isPremarketHours;
    }

}
