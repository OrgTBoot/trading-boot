package com.mg.trading.boot.domain.indicators.supertrend;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

@Log4j2
public class SuperTrendBuyIndicator extends CachedIndicator<Boolean> {
    final SuperTrendIndicator indicator;

    public SuperTrendBuyIndicator(BarSeries series, int length) {
        super(series);
        this.indicator = new SuperTrendIndicator(super.getBarSeries(), 3D, length);
    }

    @Override
    protected Boolean calculate(int index) {
        boolean result = "BUY".equals(indicator.getSignal(index));
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, result);
        return result;
    }
}
