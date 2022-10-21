package com.mg.trading.boot.strategy;

import com.mg.trading.boot.domain.exceptions.ValidationException;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
public class TradingStrategyExecutorHolder {
    private final static Map<String, StrategyExecutor> context = new HashMap<>();


    public synchronized static void add(StrategyExecutor strategyExecutor) {
        String key = getStrategyKey(strategyExecutor);

        if (context.containsKey(key)) {
            throw new ValidationException("This strategy is already running: " + key);
        }
        context.put(key, strategyExecutor);
    }

    public synchronized static void remove(String key) {
        StrategyExecutor strategyExecutor = context.get(key);
        if (strategyExecutor != null) {
            log.info("Stop signal sent to strategy with key {}", key);
            strategyExecutor.stop();
            context.remove(key);

        } else {
            log.info("Strategy key {} not found in {}", key, context.keySet());
        }
    }

    public synchronized static Set<String> getRunningKeys() {
        return context.keySet();
    }

    private synchronized static String getStrategyKey(StrategyExecutor executor) {
        return executor.getContext().getStrategy().getName().toUpperCase();
    }
}
