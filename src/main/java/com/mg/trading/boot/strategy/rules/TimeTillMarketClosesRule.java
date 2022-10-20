package com.mg.trading.boot.strategy.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.strategy.indicators.AbstractMarketHoursIndicator.getMinutesTillMarketClose;


@Log4j2
public class TimeTillMarketClosesRule implements Rule {
    private final BarSeries series;
    private final Integer value;
    private final TimeUnit timeUnit;

    public TimeTillMarketClosesRule(BarSeries series, Integer value, TimeUnit timeUnit) {
        this.series = series;
        this.value = value;
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Bar bar = series.getBar(index);

        long thresholdMinutes = timeUnit.convert(value, TimeUnit.MINUTES);
        long remainingMinutes = getMinutesTillMarketClose(bar);

        boolean reachedThreshold = remainingMinutes <= thresholdMinutes;
        if (reachedThreshold) {
            log.warn("There are {} minutes till marked closes. Idx={}", remainingMinutes, index);
        }

        return reachedThreshold;
    }
}
