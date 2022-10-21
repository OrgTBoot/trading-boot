package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.supertrend.XSuperTrendSellIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class XSuperTrendSellRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final Integer barsCount;

    public XSuperTrendSellRule(BarSeries series, Integer barsCount) {
        this.series = series;
        this.barsCount = barsCount;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        XSuperTrendSellIndicator indicator = new XSuperTrendSellIndicator(series, barsCount);
        Boolean satisfied = indicator.calculate(index);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
