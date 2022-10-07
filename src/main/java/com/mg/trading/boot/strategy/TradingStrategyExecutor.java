package com.mg.trading.boot.strategy;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.TickerQuoteExtractor;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TradingStrategyExecutor implements StrategyExecutor {

    private final com.mg.trading.boot.models.StrategyContext context;
    private final ScheduledExecutorService executor;


    public TradingStrategyExecutor(com.mg.trading.boot.models.StrategyContext context) {
        this.context = context;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        validateContext(context);
        log.info("\tParameters: {}", context.getParameters());

        StrategyParameters parameters = context.getParameters();
        Integer frequency = parameters.getQuotesPullFrequencyInSec();
        String strategyName = context.getStrategy().getName();
        TickerQuoteExtractor listener = context.getQuoteExtractor();

        executor.scheduleAtFixedRate(listener, 0, frequency, TimeUnit.SECONDS);
        log.info("\tStrategy {} is running...", strategyName);
        log.info("------------------------------------------------");
    }

    public void stop() {
        executor.shutdownNow();
        log.info("Trading strategy {} for symbol {} is stopped",
                context.getStrategy().getName(),
                context.getParameters().getSymbol());
    }

    private void validateContext(com.mg.trading.boot.models.StrategyContext context) {
        Assert.notNull(context, "Strategy context should not be null");
        Assert.notNull(context.getSeries(), "Series should not be null");
        Assert.isTrue(!context.getSeries().isEmpty(), "Series should not be empty");
        Assert.notNull(context.getStrategy(), "Strategy should not be null");
        Assert.notNull(context.getParameters(), "Parameters should not be null");
        Assert.notNull(context.getQuoteExtractor(), "Quotes Listener should not be null");
        Assert.notNull(context.getTradingRecord(), "Trading record should not be null");

        Number stopLoss = context.getParameters().getStopLossPercent();
        Number stopGain = context.getParameters().getStopGainPercent();
        Assert.notNull(stopGain, "Parameters stop gain should not be null");
        Assert.notNull(stopLoss, "Parameters stop loss should not be null");
        Assert.isTrue(stopGain.doubleValue() > stopLoss.doubleValue(), "Parameters stop loss < gain");
        Assert.notNull(context.getParameters().getSymbol(), "Parameters symbol should not be null");
        Assert.notNull(context.getParameters().getSharesQty(), "Parameters shares qty should not be null");
        Assert.notNull(context.getParameters().getQuotesRange(), "Parameters range should not be null");
        Assert.notNull(context.getParameters().getQuotesInterval(), "Parameters interval should not be null");
        Assert.notNull(context.getParameters().getQuotesPullRange(), "Parameters pull interval should not be null");
        Assert.notNull(context.getParameters().getQuotesRollingLimit(), "Parameters rolling limit should not be null");
        Assert.notNull(context.getParameters().getQuotesPullFrequencyInSec(), "Parameters pull frequency should not be null");
    }
}
