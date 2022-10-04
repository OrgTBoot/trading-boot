package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.data.Interval;
import com.mg.trading.boot.data.TradingPeriod;
import com.mg.trading.boot.strategy.indicators.AfterMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.codehaus.groovy.runtime.callsite.BooleanReturningMethodInvoker;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.FixedBooleanIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;


/**
 * Apply two moving averages to a chart: one longer and one shorter.
 * When the shorter-term MA crosses above the longer-term MA, it's a buy signal, as it indicates that the trend is
 * shifting up. This is known as a golden cross. Meanwhile, when the shorter-term MA crosses below the
 * longer-term MA, it's a sell signal, as it indicates that the trend is shifting down
 * <p>
 * For more details see: <a href="https://www.youtube.com/watch?v=6mckJdktXkc">Golden Cross</a>
 */
@Log4j2
public class GoldenCrossStrategyInitializer {

    public static Strategy init(GoldenCrossStrategyParameters parameters, BarSeries series) {
        if (series == null || series.isEmpty()) {
            throw new RuntimeException("Series are not expected to be empty.");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortEmaInd = new EMAIndicator(closePrice, parameters.getShortEMABarCount());
        EMAIndicator longEmaInd = new EMAIndicator(closePrice, parameters.getLongEMABarCount());

        //enter rules
        CrossedUpIndicatorRule crossedUpEMA = new CrossedUpIndicatorRule(shortEmaInd, longEmaInd);
        BooleanIndicatorRule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series)); //enter only during market hours
        Rule enterRule = crossedUpEMA.and(marketHours);

        //exit rules
        StopLossRule stopLoss = new StopLossRule(closePrice, parameters.getStopLossPercent());
        StopGainRule stopGain = new StopGainRule(closePrice, parameters.getStopGainPercent());
        BooleanIndicatorRule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
        StopGainRule hasProfit = new StopGainRule(closePrice, 0.1);

        Rule exitRule = stopLoss.or(stopGain).or(afterMarketHours.and(hasProfit));

        return new BaseStrategy("GoldenCrossStrategy", enterRule, exitRule);
    }

//    private static void printS(BarSeries series) {
//        for (int i = 0; i <= series.getEndIndex(); i++) {
//            final Bar bar = series.getBar(i);
//            log.info("bar idx={} marketH={}, marketAft={}, close={} dt={}",
//                    i,
//                    isMarketHours(bar),
//                    isAfterMarketHours(bar),
//                    bar.getClosePrice(),
//                    bar.getEndTime());
//        }
//    }

    public static GoldenCrossStrategyParameters params(String symbol) {
        return GoldenCrossStrategyParameters.builder()
                .symbol(symbol.toUpperCase())
                .sharesQty(BigDecimal.ONE)
                .stopLossPercent(2)
                .stopGainPercent(3)
                .longEMABarCount(30)
                .shortEMABarCount(5)
                .tradingPeriod(TradingPeriod.EXTENDED)
                .quotesInterval(Interval.ONE_MINUTE) //bar interval, ex: open-close interval of the bar is 1 minute
                .quotesRollingLimit(60) //keep total of 60 rolling bars. Update for backtracking, ex: 360 (6hours).
                .quotesPullLimit(5) // limit quotes pull to 5 elements. In other words last 5 minutes, 5 bars/quotes
                .quotesPullFrequencyInSec(10) // pull refreshed quotes each 10 seconds
                .build();
    }

//    /**
//     * Pre marked hours are considered to be between 4:00AM - 9:30AM EST
//     *
//     * @param series - bar series
//     * @return - true if last bar is in pre-market hours timeframe
//     */
//    private static boolean isPreMarketHours(BarSeries series) {
//        ZonedDateTime lastBarDateTime = series.getLastBar().getEndTime();
//        int hour = lastBarDateTime.getHour();
//        int minute = lastBarDateTime.getMinute();
//
//        return (hour >= 4) && (hour < 9 || hour == 9 && minute < 30);
//    }

//    /**
//     * Pre marked hours are considered to be between 4:00AM - 4:00PM EST
//     *
//     * @param series - bar series
//     * @return - true if last bar is in pre-market hours timeframe
//     */
//    private static boolean isMarketHours(Bar bar) {
//        ZonedDateTime lastBarDateTime = bar.getEndTime();
//        int hour = lastBarDateTime.getHour();
//        int minute = lastBarDateTime.getMinute();
//
////        return (hour > 9 || hour == 9 && minute > 30) && hour < 16;
//        return (hour >= 4 && hour < 16);
//    }
//
//    /**
//     * Pre marked hours are considered to be between 4:00PM - 8:00PM EST
//     *
//     * @param series - bar series
//     * @return - true if last bar is in pre-market hours timeframe
//     */
//    private static boolean isAfterMarketHours(BarSeries series) {
//        return isAfterMarketHours(series.getLastBar());
//    }
//
//    private static boolean isAfterMarketHours(Bar bar) {
//        ZonedDateTime lastBarDateTime = bar.getEndTime();
//        int hour = lastBarDateTime.getHour();
//        return hour >= 16 && hour < 20;
//    }
}
