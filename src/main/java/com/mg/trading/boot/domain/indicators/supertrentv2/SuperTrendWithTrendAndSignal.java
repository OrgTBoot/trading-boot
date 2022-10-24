package com.mg.trading.boot.domain.indicators.supertrentv2;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public class SuperTrendWithTrendAndSignal extends AbstractSuperTrend<Triple<Trend, Signal, Num>> {
    public SuperTrendWithTrendAndSignal(BarSeries series, int barCount, int multiplier) {
        super(series, barCount, multiplier);
    }

    @Override
    public Triple<Trend, Signal, Num> getValue(int index) {
        Pair<Trend, Num> actual = calculate(index);
        Pair<Trend, Num> previous = calculate(index - 1);

        Signal signal = Signal.NO_SIGNAL;

        if (Trend.UP.equals(previous.getLeft()) && Trend.DOWN.equals(actual.getLeft())) {
            signal = Signal.DOWN;
        } else {
            if (Trend.DOWN.equals(previous.getLeft()) && Trend.UP.equals(actual.getLeft())) {
                signal = Signal.UP;
            }
        }

        return Triple.of(actual.getLeft(), signal, actual.getRight());
    }
}
