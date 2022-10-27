package com.mg.trading.boot.domain.executors;

import com.mg.trading.boot.domain.strategy.StrategyDefinition;

import java.util.Set;

public interface StrategyExecutor {

    void start(StrategyDefinition strategyDef);

    void stop(String strategyKey);

    Set<String> getRunningStrategyKeys();
}
