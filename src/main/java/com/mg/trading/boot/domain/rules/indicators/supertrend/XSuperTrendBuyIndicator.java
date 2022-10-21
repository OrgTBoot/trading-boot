package com.mg.trading.boot.domain.rules.indicators.supertrend;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

@Log4j2
public class XSuperTrendBuyIndicator extends CachedIndicator<Boolean> {
    final XSuperTrendIndicator indicator;

    public XSuperTrendBuyIndicator(BarSeries series, int length) {
        super(series);
        this.indicator = new XSuperTrendIndicator(super.getBarSeries(), 3D, length);
    }

    @Override
    protected Boolean calculate(int index) {
        boolean result = "BUY".equals(indicator.getSignal(index));
//        log.info("Idx {} isGreen {} signal={}", index, result, indicator.getSignal(index));
        return result;
    }
}
