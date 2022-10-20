package com.mg.trading.boot.strategy.core;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface StrategyProvider {

    StrategyProvider buildStrategy(BarSeries series);

    Strategy getStrategy();

    StrategyParameters getParams();
}
