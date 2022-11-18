package com.mg.trading.boot.domain.tasks;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.domain.order.QuoteChangeListener;
import com.mg.trading.boot.integrations.BrokerProvider;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class QuoteExtractorTask implements Runnable {
    private final BrokerProvider broker;
    private final StrategyDefinition strategyDef;
    private final List<QuoteChangeListener> subscribers;

    public QuoteExtractorTask(final StrategyDefinition strategyDef, final BrokerProvider broker) {
        this.strategyDef = strategyDef;
        this.broker = broker;
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            String symbol = strategyDef.getSymbol();
            Range range = strategyDef.getParams().getQuotesRange();
            Interval interval = strategyDef.getParams().getQuotesInterval();

            List<TickerQuote> quotes = broker.ticker().getTickerQuotes(symbol, range, interval);
            List<TickerQuote> fullQuotes = quotes.subList(0, quotes.size() - 2); //we always want to ignore last quote as it is not full
            strategyDef.updateSeries(fullQuotes);
            notifySubscribers();
        } catch (Exception e) {
            log.error("Unexpected error in Quote extractor: {}", e.getMessage());
        }
    }

    private void notifySubscribers() {
        subscribers.forEach(it -> it.onQuoteChange(strategyDef, broker));
    }


    public void subscribe(QuoteChangeListener subscriber) {
        this.subscribers.add(subscriber);
    }
}
