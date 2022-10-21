package com.mg.trading.boot.strategy.core;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.domain.models.TickerQuote;
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

        List<TickerQuote> quotes = brokerProvider.ticker().getTickerQuotes(
                params.getSymbol(),
                params.getQuotesRange(),
                params.getQuotesInterval());

        Duration quoteDuration = getTickerDuration(quotes);

//        if (quoteDuration.getSeconds() != params.getQuotesInterval().seconds) {
//            throw new RuntimeException("Extracted quotes interval " + quoteDuration.getSeconds()
//                    + " are not consistent with expected interval " + params.getQuotesInterval().seconds);
//        }

        BarSeriesUtils.addBarSeries(series, quotes, quoteDuration);
        log.info("\tInitialized {} series. range {}, interval {} - OK",
                series.getBarCount(),
                params.getQuotesRange(),
                params.getQuotesInterval());
        return series;
    }

    private static Duration getTickerDuration(List<TickerQuote> quotes) {
        int lastQuoteIdx = quotes.size() - 1;
        return Duration.ofSeconds(quotes.get(lastQuoteIdx).getTimeStamp() - quotes.get(lastQuoteIdx - 1).getTimeStamp());
    }

}
