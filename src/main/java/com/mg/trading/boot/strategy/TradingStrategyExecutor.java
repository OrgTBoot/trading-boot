package com.mg.trading.boot.strategy;

import com.mg.trading.boot.models.StrategyContext;
import com.mg.trading.boot.strategy.core.StrategyTickerListenerInitializer;
import com.mg.trading.boot.strategy.core.QuoteListener;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Log4j2
public class TradingStrategyExecutor implements StrategyExecutor {

    private final StrategyContext context;
    private final ScheduledExecutorService executor;


    public TradingStrategyExecutor(StrategyContext context) {
        validateContext(context);
        this.context = context;
        this.executor = getScheduledExecutorService();
    }

    @Override
    public void start() {
        log.info("\tParameters: {}", context.getParameters());

        Integer frequency = context.getParameters().getQuotesPullFrequencyInSec();
        QuoteListener listener = getQuoteListener();

        executor.scheduleAtFixedRate(listener, 0, frequency, TimeUnit.SECONDS);
        log.info("\tStrategy {} is running...", context.getStrategy().getName());
        log.info("------------------------------------------------");
    }

    @Override
    public void stop() {
        executor.shutdownNow();
        log.info("Trading strategy {} for symbol {} is stopped",
                context.getStrategy().getName(),
                context.getParameters().getSymbol());
    }

    @Override
    public StrategyContext getContext() {
        return this.context;
    }

    private void validateContext(StrategyContext context) {
        checkNotNull(context, "Strategy context should not be null");
        checkNotNull(context, "Strategy context should not be null");
        checkNotNull(context.getBroker(), "Broker should not be null");
        checkNotNull(context.getSeries(), "Series should not be null");
        checkState(!context.getSeries().isEmpty(), "Series should not be empty");
        checkNotNull(context.getStrategy(), "Strategy should not be null");
        checkNotNull(context.getParameters(), "Parameters should not be null");

        Number stopLoss = context.getParameters().getPositionStopLossPercent();
        checkNotNull(stopLoss, "Parameters stop loss should not be null");
        checkNotNull(context.getParameters().getSymbol(), "Parameters symbol should not be null");
        checkNotNull(context.getParameters().getSharesQty(), "Parameters shares qty should not be null");
        checkNotNull(context.getParameters().getQuotesRange(), "Parameters range should not be null");
        checkNotNull(context.getParameters().getQuotesInterval(), "Parameters interval should not be null");
        checkNotNull(context.getParameters().getQuotesRollingLimit(), "Parameters rolling limit should not be null");
        checkNotNull(context.getParameters().getQuotesPullFrequencyInSec(), "Parameters pull frequency should not be null");
    }

    private QuoteListener getQuoteListener() {
        return StrategyTickerListenerInitializer.init(
                context.getBroker(),
                context.getParameters(),
                context.getStrategy(),
                context.getSeries());
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        ThreadFactory threadFactory = runnable -> new Thread(runnable, context.getStrategy().getName());
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}
