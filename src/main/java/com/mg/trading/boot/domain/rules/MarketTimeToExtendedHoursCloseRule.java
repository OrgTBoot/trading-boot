package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.indicators.markethours.AbstractMarketHoursIndicator.getMinutesTillMarketExtendedHoursClose;


@Log4j2
public class MarketTimeToExtendedHoursCloseRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final Integer value;
    private final TimeUnit timeUnit;

    public MarketTimeToExtendedHoursCloseRule(BarSeries series, Integer value, TimeUnit timeUnit) {
        this.series = series;
        this.value = value;
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Bar bar = series.getBar(index);

        long thresholdMinutes = timeUnit.convert(value, TimeUnit.MINUTES);
        long remainingMinutes = getMinutesTillMarketExtendedHoursClose(bar);

        boolean reachedThreshold = remainingMinutes <= thresholdMinutes;
        if (reachedThreshold) {
            log.trace("There are {} minutes till marked extended hours close. Idx={}", remainingMinutes, index);
        }

        traceIsSatisfied(index, reachedThreshold);
        return reachedThreshold;
    }
}
