package com.mg.trading.boot.domain.strategy;

import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.rules.TracingRule;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Rule;

import java.time.Duration;
import java.util.List;

@Log4j2
public abstract class AbstractStrategyDefinition implements IStrategyDefinition {
    private final String symbol;
    private final String strategyPrefix;
    protected final BarSeries series;

    public AbstractStrategyDefinition(String symbol, String strategyPrefix) {
        this.symbol = symbol.toUpperCase();
        this.strategyPrefix = strategyPrefix.toUpperCase();
        this.series = initSeries();
    }

    public String getStrategyName() {
        return strategyPrefix + "_" + symbol;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public BarSeries getSeries() {
        return series;
    }

    @Override
    public BarSeries updateSeries(List<TickerQuote> quotes) {
        if (!CollectionUtils.isEmpty(quotes)) {
            //there is a high possibility that the last quote is not a full bar, we always ignore it.
            List<TickerQuote> fullQuotes = quotes.subList(0, quotes.size() - 1);
            upsertSeries(fullQuotes);
        }

        return series;
    }

    protected Rule trace(Rule rule) {
        return new TracingRule(rule, TracingRule.Type.CHAIN);
    }

    protected Rule trace(Rule rule, TracingRule.Type type) {
        return new TracingRule(rule, type);
    }

    protected Rule trace(Rule rule, String alias) {
        return new TracingRule(rule, TracingRule.Type.CHAIN, alias);
    }

    private BarSeries initSeries() {
        return new BaseBarSeriesBuilder().withName(getStrategyName()).withMaxBarCount(1000).build();
    }

    private static Duration getQuoteDuration(List<TickerQuote> quotes) {
        int lastQuoteIdx = quotes.size() - 1;
        return Duration.ofSeconds(quotes.get(lastQuoteIdx).getTimeStamp() - quotes.get(lastQuoteIdx - 1).getTimeStamp());
    }

    private void upsertSeries(List<TickerQuote> quotes) {
        Duration duration = getQuoteDuration(quotes);
        for (TickerQuote quote : quotes) {

            if (series.getBarCount() == 0) {
                BarSeriesUtils.addBar(series, quote, duration);

            } else if (shouldReplaceBar(series, quote)) {
                BarSeriesUtils.addBar(series, quote, duration, Boolean.TRUE);
                log.debug("Replaced Quote : {}", quote);

            } else if (shouldAddBar(series, quote)) {
                BarSeriesUtils.addBar(series, quote, duration);
                log.debug("Added Quote : {}", quote);
            }
        }
    }

    private boolean shouldReplaceBar(BarSeries series, TickerQuote quote) {
        Bar lastBar = series.getLastBar();
        long latestBarTimeStamp = lastBar.getEndTime().toInstant().getEpochSecond();
        boolean sameTimeStamp = quote.getTimeStamp().equals(latestBarTimeStamp);
        boolean sameVolume = quote.getVolume().equals(lastBar.getVolume().longValue());
        boolean sameClosePrice = quote.getClosePrice().doubleValue() == lastBar.getClosePrice().doubleValue();

        return sameTimeStamp && (!sameVolume || !sameClosePrice);
    }

    private boolean shouldAddBar(BarSeries series, TickerQuote quote) {
        Bar lastBar = series.getLastBar();
        long latestBarTimeStamp = lastBar.getEndTime().toInstant().getEpochSecond();
        boolean hasVolume = quote.getVolume() != 0;

        return quote.getTimeStamp() > latestBarTimeStamp && hasVolume;
    }
}
