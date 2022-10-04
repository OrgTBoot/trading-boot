package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.data.TickerQuote;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class TickerQuoteExtractor implements Runnable {

    private final Supplier<List<TickerQuote>> quotesSupplier;
    private final Supplier<Void> onChangeDecisionSupplier;
    private final BarSeries series;

    public TickerQuoteExtractor(final Supplier<List<TickerQuote>> quotesSupplier,
                                final Supplier<Void> onChangeDecisionSupplier,
                                final BarSeries series) {
        this.quotesSupplier = quotesSupplier;
        this.onChangeDecisionSupplier = onChangeDecisionSupplier;
        this.series = series;
    }


    @Override
    public void run() {
        try {
            List<TickerQuote> quotes = quotesSupplier.get();
            Duration duration = getDuration(series);
            for (TickerQuote quote : quotes) {
                if (shouldReplace(series, quote)) {
                    BarSeriesUtils.addBar(series, quote, duration, Boolean.TRUE);
                    log.info("Replaced Quote : {}", quote);
                    this.onChangeDecisionSupplier.get(); //trigger buy/sell logic

                } else if (shouldAdd(series, quote)) {
                    BarSeriesUtils.addBar(series, quote, duration, Boolean.FALSE);
                    log.info("Added Quote : {}", quote);
                    this.onChangeDecisionSupplier.get(); //trigger buy/sell logic

                }
//                else {
//                    log.debug("Ignored quote {}", quote);
//                }
            }
        } catch (Throwable e) {
            log.error("Error in quote processing: " + e.getMessage(), e);
        }
    }


    private boolean shouldReplace(BarSeries series, TickerQuote quote) {
        Bar lastBar = series.getLastBar();
        long latestBarTimeStamp = lastBar.getEndTime().toInstant().getEpochSecond();
        boolean sameTimeStamp = quote.getTimeStamp().equals(latestBarTimeStamp);
        boolean sameVolume = quote.getVolume().equals(lastBar.getVolume().longValue());
        boolean sameClosePrice = quote.getClosePrice().doubleValue() == lastBar.getClosePrice().doubleValue();

        return sameTimeStamp && (!sameVolume || !sameClosePrice);
    }

    private boolean shouldAdd(BarSeries series, TickerQuote quote) {
        Bar lastBar = series.getLastBar();
        long latestBarTimeStamp = lastBar.getEndTime().toInstant().getEpochSecond();
        return quote.getTimeStamp() > latestBarTimeStamp;
    }

    private Duration getDuration(BarSeries series) {
        return series.getLastBar().getTimePeriod();
    }

}
