package com.mg.trading.boot.domain.rules.indicators.supertrend;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;

@Log4j2
public class XSuperTrendSellIndicator extends CachedIndicator<Boolean> {
    final XSuperTrendIndicator indicator;

    public XSuperTrendSellIndicator(BarSeries series, int length) {
        super(series);
        this.indicator = new XSuperTrendIndicator(super.getBarSeries(), 3D, length);
    }

    @Override
    public Boolean calculate(int index) {
        return "SELL".equals(indicator.getSignal(index));
    }
}
