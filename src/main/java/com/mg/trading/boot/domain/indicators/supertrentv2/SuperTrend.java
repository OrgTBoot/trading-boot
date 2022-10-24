package com.mg.trading.boot.domain.indicators.supertrentv2;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public class SuperTrend extends AbstractSuperTrend<Num> {

    public SuperTrend(BarSeries series, int barCount, int multiplier) {
        super(series, barCount, multiplier);
    }

    @Override
    public Num getValue(int index) {
        return calculate(index).getRight();
    }
}
