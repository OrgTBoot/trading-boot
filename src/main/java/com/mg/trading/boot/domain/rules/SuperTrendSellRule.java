package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.supertrend.SuperTrendSellIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class SuperTrendSellRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final Integer barsCount;

    public SuperTrendSellRule(BarSeries series, Integer barsCount) {
        this.series = series;
        this.barsCount = barsCount;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        SuperTrendSellIndicator indicator = new SuperTrendSellIndicator(series, barsCount);
        Boolean satisfied = indicator.calculate(index);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
