package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface StrategyProvider {

    StrategyProvider buildStrategy(BarSeries series);

    Strategy getStrategy();

    StrategyParameters getParameters();
}
