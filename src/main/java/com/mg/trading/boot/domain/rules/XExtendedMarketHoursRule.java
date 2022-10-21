package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.XExtendedMarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class XExtendedMarketHoursRule extends AbstractRule implements Rule {
    private final BarSeries series;

    public XExtendedMarketHoursRule(BarSeries series) {
        this.series = series;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        XExtendedMarketHoursIndicator indicator = new XExtendedMarketHoursIndicator(series);
        Boolean satisfied = indicator.calculate(index);

        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
