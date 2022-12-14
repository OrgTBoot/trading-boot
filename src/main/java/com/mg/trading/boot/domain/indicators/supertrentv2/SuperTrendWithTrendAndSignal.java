package com.mg.trading.boot.domain.indicators.supertrentv2;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

public class SuperTrendWithTrendAndSignal extends AbstractSuperTrend<Triple<Trend, Signal, Num>> {
    public SuperTrendWithTrendAndSignal(BarSeries series, int barCount, Double multiplier) {
        super(series, barCount, multiplier);
    }

    @Override
    public Triple<Trend, Signal, Num> getValue(int index) {
        Signal signal = Signal.NO_SIGNAL;
        Pair<Trend, Num> actual = calculate(index);

        if (index <= 0) {
            return Triple.of(actual.getLeft(), signal, actual.getRight());
        }

        Pair<Trend, Num> previous = calculate(index - 1);

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
