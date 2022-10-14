package com.mg.trading.boot.graphql;

import com.mg.trading.boot.exceptions.ValidationException;
import com.mg.trading.boot.strategy.StrategyExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RunningStrategiesHolder {
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
            strategyExecutor.stop();
            context.remove(key);
        }
    }

    public synchronized static Set<String> getRunningKeys() {
        return context.keySet();
    }

    private synchronized static String getStrategyKey(StrategyExecutor executor) {
        return executor.getContext().getStrategy().getName();
    }
}
