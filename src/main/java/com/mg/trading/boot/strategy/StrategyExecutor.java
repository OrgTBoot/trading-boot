package com.mg.trading.boot.strategy;

import com.mg.trading.boot.domain.models.StrategyContext;

public interface StrategyExecutor {

    void start();

    void stop();

    StrategyContext getContext();
}
