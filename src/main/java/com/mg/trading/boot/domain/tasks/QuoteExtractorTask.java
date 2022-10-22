package com.mg.trading.boot.domain.tasks;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.subscribers.QuteChangeListener;
import com.mg.trading.boot.integrations.BrokerProvider;

import java.util.ArrayList;
import java.util.List;

public class QuoteExtractorTask implements Runnable {
    private final BrokerProvider broker;
    private final IStrategyDefinition strategyDef;
    private List<QuteChangeListener> subscribers;

    public QuoteExtractorTask(final IStrategyDefinition strategyDef, final BrokerProvider broker) {
        this.strategyDef = strategyDef;
        this.broker = broker;
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void run() {
        String symbol = strategyDef.getSymbol();
        Range range = strategyDef.getParams().getQuotesRange();
        Interval interval = strategyDef.getParams().getQuotesInterval();

        List<TickerQuote> quotes = broker.ticker().getTickerQuotes(symbol, range, interval);
        strategyDef.updateSeries(quotes);
        notifySubscribers();
    }

    private void notifySubscribers() {
        subscribers.forEach(it -> it.onQuoteChange(strategyDef, broker));
    }


    public void subscribe(QuteChangeListener subscriber) {
        this.subscribers.add(subscriber);
    }
}