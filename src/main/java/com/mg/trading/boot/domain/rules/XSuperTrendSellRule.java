package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.XPreMarketHoursIndicator;
import com.mg.trading.boot.domain.rules.indicators.supertrend.XSuperTrendSellIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;

@Log4j2
public class XSuperTrendSellRule implements IRule {
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
        logResult(log, satisfied, index);

        return satisfied;
    }
}
