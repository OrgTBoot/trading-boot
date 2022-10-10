package com.mg.trading.boot.strategy.indicators;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

@Log4j2
public class SuperTrendSellIndicator extends CachedIndicator<Boolean> {
    final SuperTrendIndicator indicator;

    public SuperTrendSellIndicator(BarSeries series, int length) {
        super(series);
        this.indicator = new SuperTrendIndicator(super.getBarSeries(), 3D, length);
    }

    @Override
    protected Boolean calculate(int index) {
        boolean result = "SELL".equals(indicator.getSignal(index));
//        log.info("Idx {} isGreen {} signal={}", index, result, indicator.getSignal(index));
        return result;
    }
}