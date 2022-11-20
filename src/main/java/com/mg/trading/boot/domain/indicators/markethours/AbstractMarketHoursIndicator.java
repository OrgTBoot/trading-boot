package com.mg.trading.boot.domain.indicators.markethours;

import org.ta4j.core.Bar;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public abstract class AbstractMarketHoursIndicator {

    /**
     * Pre-Market hours are considered to be between 4:00AM - 9:30AM EST
     *
     * @param bar - bar to evaluate
     * @return - true if bar time is in market hours timeframe
     */
    public static boolean isPremarketHours(Bar bar) {
        int hour = getHour(bar);
        int minute = getMinute(bar);

        return hour >= 4 && hour < 9 || (hour == 9 && minute < 30);
    }

    /**
     * Market hours are considered to be between 9:30AM - 4:00PM EST
     *
     * @param bar - bar to evaluate
     * @return - true if bar time is in market hours timeframe
     */
    public static boolean isMarketHours(Bar bar) {
        int hour = getHour(bar);
        int minute = getMinute(bar);

        return hour > 9 && hour < 16 || (hour == 9 && minute > 30);
    }

    /**
     * After marked hours are considered to be between 4:00PM - 8:00PM EST
     *
     * @param bar - bar to evaluate
     * @return - true if bar time is in after-market hours timeframe
     */
    public static boolean isExtendedMarketHours(Bar bar) {
        int hour = getHour(bar);

        return hour >= 16 && hour < 20;
    }

//    public static long getMinutesTillMarketExtendedHoursClose(Bar bar) {
//        ZonedDateTime time = getDateTime(bar);
//        ZonedDateTime marketCloseTime = ZonedDateTime.of(
//                time.getYear(), time.getMonthValue(), time.getDayOfMonth(), 16, 0, 0, 0, time.getZone());
//
//        return ChronoUnit.MINUTES.between(time, marketCloseTime);
//    }

    public static long getMinutesTillPreMarketHoursClose(Bar bar) {
        ZonedDateTime time = getDateTime(bar);
        ZonedDateTime marketHoursStartTime = ZonedDateTime.of(
                time.getYear(), time.getMonthValue(), time.getDayOfMonth(), 9, 30, 0, 0, time.getZone());

        return ChronoUnit.MINUTES.between(time, marketHoursStartTime);
    }

    public static long getMinutesTillMarketHoursClose(Bar bar) {
        ZonedDateTime time = getDateTime(bar);
        ZonedDateTime marketHoursStartTime = ZonedDateTime.of(
                time.getYear(), time.getMonthValue(), time.getDayOfMonth(), 16, 0, 0, 0, time.getZone());

        return ChronoUnit.MINUTES.between(time, marketHoursStartTime);
    }

    public static long getMinutesTillExtendedHoursClose(Bar bar) {
        ZonedDateTime time = getDateTime(bar);
        ZonedDateTime marketHoursStartTime = ZonedDateTime.of(
                time.getYear(), time.getMonthValue(), time.getDayOfMonth(), 20, 0, 0, 0, time.getZone());

        return ChronoUnit.MINUTES.between(time, marketHoursStartTime);
    }

    private static int getHour(Bar bar) {
        ZonedDateTime time = getDateTime(bar);
        return time.getHour();
    }

    private static int getMinute(Bar bar) {
        ZonedDateTime time = getDateTime(bar);
        return time.getMinute();
    }

    private static ZonedDateTime getDateTime(Bar bar) {
        return bar.getEndTime();
    }
}
