package com.mg.trading.boot.utils;

import com.mg.trading.boot.models.TickerQuote;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class BarSeriesUtils {

    public static void addBar(BarSeries barSeries, TickerQuote quote, Duration duration, boolean replaceLast) {
        barSeries.addBar(mapToBar(quote, duration), replaceLast);
    }

    public static void addBar(BarSeries barSeries, TickerQuote quote, Duration duration) {
        //usually last quote of the day comes with volume and price as 0.
        boolean emptyQuote = quote.getVolume() == 0 && quote.getOpenPrice().doubleValue() == 0;
        if (!emptyQuote) {
            barSeries.addBar(mapToBar(quote, duration));
        }
    }

    public static void addBarSeries(BarSeries barSeries, List<TickerQuote> quotes, Duration duration) {
        quotes.forEach(it -> addBar(barSeries, it, duration));
    }

    public static Bar mapToBar(TickerQuote quote, Duration duration) {
        return new BaseBar(
                duration,
                getZonedDateTime(quote),
                quote.getOpenPrice(),
                quote.getHighPrice(),
                quote.getLowPrice(),
                quote.getClosePrice(),
                BigDecimal.valueOf(quote.getVolume()));
    }

    public static ZonedDateTime getZonedDateTime(TickerQuote quote) {
        return Instant.ofEpochSecond(quote.getTimeStamp()).atZone(ZoneId.of(quote.getTimeZone()));
    }


}
