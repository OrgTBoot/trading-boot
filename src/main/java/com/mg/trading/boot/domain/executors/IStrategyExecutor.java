package com.mg.trading.boot.domain.executors;

import com.mg.trading.boot.domain.strategy.IStrategyDefinition;

import java.util.List;
import java.util.Set;

public interface IStrategyExecutor {

    void start(IStrategyDefinition strategyDef);

    void stop(String strategyKey);

    Set<String> getRunningStrategyKeys();
}
