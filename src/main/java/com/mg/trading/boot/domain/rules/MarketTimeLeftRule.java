package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.domain.indicators.markethours.AbstractMarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

import java.util.concurrent.TimeUnit;

/**
 * Considered satisfied if reached the time of a market frame.
 * Ex: 30 minutes before MARKET_HOURS end.
 */
@Log4j2
public class MarketTimeLeftRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final Integer value;
    private final TimeUnit timeUnit;
    private final Market market;

    public MarketTimeLeftRule(BarSeries series, Market market, Integer value, TimeUnit timeUnit) {
        this.series = series;
        this.market = market;
        this.value = value;
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Bar bar = series.getBar(index);

        long thresholdMinutes = timeUnit.convert(value, TimeUnit.MINUTES);
        long remainingMinutes = getRemainingMinutes(bar, market);

        boolean reachedThreshold = remainingMinutes <= thresholdMinutes;
        if (reachedThreshold) {
            log.trace("There are {} minutes till {} close. Idx={}", market, remainingMinutes, index);
        }

        traceIsSatisfied(index, reachedThreshold);
        return reachedThreshold;
    }


    private long getRemainingMinutes(Bar bar, Market market) {
        switch (market) {
            case PRE_MARKET:
                return AbstractMarketHoursIndicator.getMinutesTillPreMarketHoursClose(bar);
            case MARKET_HOURS:
                return AbstractMarketHoursIndicator.getMinutesTillMarketHoursClose(bar);
            case AFTER_HOURS:
                return AbstractMarketHoursIndicator.getMinutesTillExtendedHoursClose(bar);
            default:
                throw new ValidationException("This type is not supported: " + market);
        }
    }

    public static enum Market {
        PRE_MARKET, MARKET_HOURS, AFTER_HOURS;
    }
}
