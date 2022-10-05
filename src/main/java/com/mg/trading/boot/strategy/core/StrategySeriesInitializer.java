package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.Duration;
import java.util.List;

@Log4j2
public class StrategySeriesInitializer {

    public static BarSeries init(BrokerProvider brokerProvider, StrategyParameters params) {
        BaseBarSeries series = new BaseBarSeriesBuilder()
                .withName(params.getSymbol())
                .withMaxBarCount(params.getQuotesRollingLimit())
                .build();

        List<TickerQuote> quotes = brokerProvider.getTickerQuotes(
                params.getSymbol(),
                params.getQuotesInterval(),
                params.getTradingPeriod(),
                params.getQuotesRollingLimit());

        Duration quoteDuration = getTickerDuration(quotes);

//        if (quoteDuration.getSeconds() != params.getQuotesInterval().seconds) {
//            throw new RuntimeException("Extracted quotes interval " + quoteDuration.getSeconds()
//                    + " are not consistent with expected interval " + params.getQuotesInterval().seconds);
//        }
        BarSeriesUtils.addBarSeries(series, quotes, quoteDuration);
        log.info("\tInitialized {} series. Interval {}, period {} - OK",
                series.getBarCount(),
                params.getQuotesInterval(),
                params.getTradingPeriod());
        return series;
    }

    private static Duration getTickerDuration(List<TickerQuote> quotes) {
        //NPE - let it be, we are expecting quotes!!!
        int lastQuoteIdx = quotes.size() - 1;
        return Duration.ofSeconds(quotes.get(lastQuoteIdx).getTimeStamp() - quotes.get(lastQuoteIdx - 1).getTimeStamp());
    }

}
