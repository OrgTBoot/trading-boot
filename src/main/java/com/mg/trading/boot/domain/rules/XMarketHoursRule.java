package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.XMarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class XMarketHoursRule extends AbstractRule implements Rule {
    private final BarSeries series;

    public XMarketHoursRule(BarSeries series) {
        this.series = series;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        XMarketHoursIndicator indicator = new XMarketHoursIndicator(series);
        Boolean satisfied = indicator.calculate(index);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
