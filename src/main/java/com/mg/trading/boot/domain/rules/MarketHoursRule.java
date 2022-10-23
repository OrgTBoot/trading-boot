package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.MarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class MarketHoursRule extends AbstractRule implements Rule {
    private final BarSeries series;

    public MarketHoursRule(BarSeries series) {
        this.series = series;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        MarketHoursIndicator indicator = new MarketHoursIndicator(series);
        Boolean satisfied = indicator.calculate(index);
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }
}
