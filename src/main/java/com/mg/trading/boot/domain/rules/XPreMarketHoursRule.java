package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.XPreMarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class XPreMarketHoursRule extends AbstractRule implements Rule {
    private final BarSeries series;

    public XPreMarketHoursRule(BarSeries series) {
        this.series = series;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        XPreMarketHoursIndicator indicator = new XPreMarketHoursIndicator(series);
        Boolean satisfied = indicator.calculate(index);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
