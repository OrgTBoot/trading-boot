package com.mg.trading.boot.domain.executors;

import com.google.common.base.Preconditions;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.subscribers.XOrderManager;
import com.mg.trading.boot.domain.tasks.XQuoteExtractorTask;
import com.mg.trading.boot.integrations.BrokerProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

@Log4j2
@Component
public class XStrategyExecutor implements IStrategyExecutor {
    private final BrokerProvider broker;
    private final XStrategyExecutorsCache strategyExecutorsCache;

    public XStrategyExecutor(final BrokerProvider broker, final XStrategyExecutorsCache strategyExecutorsCache) {
        this.broker = broker;
        this.strategyExecutorsCache = strategyExecutorsCache;
    }

    @Override
    public void start(IStrategyDefinition strategyDef) {
        validate(strategyDef);

        Integer pullFrequency = strategyDef.getParams().getQuotesPullFrequencyInSec();
        log.info("Starting strategy {}...", strategyDef.getStrategy().getName());
        log.info("\tParameters: {}", strategyDef.getParams());
        log.info("\tRange     : {}", strategyDef.getParams().getQuotesRange());
        log.info("\tInterval  : {}", strategyDef.getParams().getQuotesInterval());
        log.info("\tPull freq : {}", pullFrequency);

        XQuoteExtractorTask quoteExtractorTask = new XQuoteExtractorTask(strategyDef, broker);
        quoteExtractorTask.subscribe(new XOrderManager());

        ScheduledExecutorService executor = getScheduledExecutor(strategyDef);
        executor.scheduleAtFixedRate(quoteExtractorTask, 0, pullFrequency, TimeUnit.SECONDS);
        strategyExecutorsCache.add(strategyDef, executor);

        log.info("\tStrategy {} is running...", strategyDef.getStrategy().getName());
        log.info("------------------------------------------------\n");
    }

    @Override
    public void stop(String strategyKey) {
        ScheduledExecutorService executor = strategyExecutorsCache.get(strategyKey);

        if (executor != null) {
            log.info("Stopping strategy with key {}", strategyKey);
            executor.shutdownNow();
            strategyExecutorsCache.remove(strategyKey);
        } else {
            log.info("Strategy key {} not found in the running list if keys {}", strategyKey, getRunningStrategyKeys());
        }
    }

    @Override
    public Set<String> getRunningStrategyKeys() {
        return strategyExecutorsCache.getRunningKeys();
    }

    private ScheduledExecutorService getScheduledExecutor(IStrategyDefinition strategyDef) {
        String name = strategyDef.getStrategy().getName();
        ThreadFactory threadFactory = runnable -> new Thread(runnable, name);

        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    private void validate(IStrategyDefinition strategyDef) {
        checkState(!strategyExecutorsCache.contains(strategyDef), "This strategy & symbol combination is already running.");
    }
}
