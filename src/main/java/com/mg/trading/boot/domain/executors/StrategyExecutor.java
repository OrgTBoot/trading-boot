package com.mg.trading.boot.domain.executors;

import com.mg.trading.boot.domain.strategy.IStrategyDefinition;

import java.util.Set;

public interface StrategyExecutor {

    void start(IStrategyDefinition strategyDef);

    void stop(String strategyKey);

    Set<String> getRunningStrategyKeys();
}
