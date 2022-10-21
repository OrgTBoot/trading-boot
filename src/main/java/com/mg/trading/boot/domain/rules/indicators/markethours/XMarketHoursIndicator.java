package com.mg.trading.boot.domain.rules.indicators.markethours;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;


@Log4j2
public class XMarketHoursIndicator extends CachedIndicator<Boolean> {

    public XMarketHoursIndicator(BarSeries series) {
        super(series);
    }

    @Override
    public Boolean calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        boolean isMktHours = XAbstractMarketHoursIndicator.isMarketHours(bar);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isMktHours);

        return isMktHours;
    }

}
