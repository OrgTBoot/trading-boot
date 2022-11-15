package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class VolumeOverRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final long volume;

    public VolumeOverRule(BarSeries series, long volume) {
        this.series = series;
        this.volume = volume;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Bar bar = series.getBar(index);
        boolean satisfied = bar.getVolume().longValue() > this.volume;
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
