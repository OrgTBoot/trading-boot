package com.mg.trading.boot.domain.strategy;

import com.google.common.annotations.VisibleForTesting;
import com.mg.trading.boot.domain.models.TickerQuote;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.List;

public interface StrategyDefinition {
    String getSymbol();

    Parameters getParams();

    BarSeries getSeries();


    @VisibleForTesting
    void setSeries(BarSeries series);

    BarSeries updateSeries(List<TickerQuote> quotes);

    Strategy getStrategy();
}
