package com.mg.trading.boot.domain.rules.indicators.supertrend;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

@Log4j2
public class XSuperTrendGreenIndicator extends CachedIndicator<Boolean> {
    final XSuperTrendIndicator indicator;

    public XSuperTrendGreenIndicator(BarSeries series, int length) {
        super(series);
        this.indicator = new XSuperTrendIndicator(super.getBarSeries(), 3D, length);
    }

    @Override
    protected Boolean calculate(int index) {
        boolean isGreen = indicator.getIsGreen(index);
        log.trace("{}#calculate({}) -> {}", this.getClass().getSimpleName(), index, isGreen);
        return isGreen;
    }
}
