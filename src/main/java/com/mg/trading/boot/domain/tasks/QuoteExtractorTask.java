package com.mg.trading.boot.domain.tasks;

import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.domain.order.QuoteChangeListener;
import com.mg.trading.boot.integrations.BrokerProvider;

import java.util.ArrayList;
import java.util.List;

public class QuoteExtractorTask implements Runnable {
    private final BrokerProvider broker;
    private final StrategyDefinition strategyDef;
    private List<QuoteChangeListener> subscribers;

    public QuoteExtractorTask(final StrategyDefinition strategyDef, final BrokerProvider broker) {
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


    public void subscribe(QuoteChangeListener subscriber) {
        this.subscribers.add(subscriber);
    }
}
