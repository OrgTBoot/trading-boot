package com.mg.trading.boot.domain.executors;

import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

@Log4j2
@Component
public class XStrategyExecutorsCache {
    private final static Map<String, ScheduledExecutorService> cache = new HashMap<>();


    public synchronized void add(IStrategyDefinition strategyDef, ScheduledExecutorService executor) {
        String key = getStrategyKey(strategyDef);

        if (cache.containsKey(key)) {
            throw new ValidationException("This strategy is already scheduled: " + key);
        }
        cache.put(key, executor);
    }

    public synchronized ScheduledExecutorService get(String key) {
        return cache.get(key);
    }

    public synchronized boolean contains(IStrategyDefinition strategyDef) {
        return cache.containsKey(getStrategyKey(strategyDef));
    }

    public synchronized void remove(String key) {
        cache.remove(key);
    }

    public synchronized Set<String> getRunningKeys() {
        return cache.keySet();
    }

    private synchronized static String getStrategyKey(IStrategyDefinition strategyDefinition) {
        return strategyDefinition.getStrategy().getName().toUpperCase();
    }
}
