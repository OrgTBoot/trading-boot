package com.mg.trading.boot.domain.rules.indicators.markethours;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;


@Log4j2
public class MarketHoursIndicator extends CachedIndicator<Boolean> {

    public MarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        boolean isMktHours = AbstractMarketHoursIndicator.isMarketHours(bar);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isMktHours);

        return isMktHours;
    }

}
