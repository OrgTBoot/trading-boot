
package com.mg.trading.boot.domain.indicators.supertrentv2;

import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public class SuperTrendWithTrend extends AbstractSuperTrend<Pair<Trend, Num>> {
    public SuperTrendWithTrend(BarSeries series, int barCount, Double multiplier) {
        super(series, barCount, multiplier);
    }

    @Override
    public Pair<Trend, Num> getValue(int index) {
        return calculate(index);
    }
}
