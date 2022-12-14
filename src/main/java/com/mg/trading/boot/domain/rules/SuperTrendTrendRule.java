package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrendWithTrendAndSignal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Triple;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class SuperTrendTrendRule extends AbstractRule implements Rule {
    private final SuperTrendWithTrendAndSignal indicator;
    private final Trend expectedTrend;

    public SuperTrendTrendRule(BarSeries series, Integer barsCount, Trend expectedTrend) {
        this.expectedTrend = expectedTrend;
        this.indicator = new SuperTrendWithTrendAndSignal(series, barsCount, 3D);
    }


    public SuperTrendTrendRule(BarSeries series, Integer barsCount, Trend expectedTrend, Double multiplier) {
        this.expectedTrend = expectedTrend;
        this.indicator = new SuperTrendWithTrendAndSignal(series, barsCount, multiplier);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {

        Triple<Trend, Signal, Num> resultEntry = this.indicator.getValue(index);
        Trend trend = resultEntry.getLeft();
        Signal signal = resultEntry.getMiddle();
        Num number = resultEntry.getRight();

        boolean satisfied = expectedTrend.equals(trend);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
