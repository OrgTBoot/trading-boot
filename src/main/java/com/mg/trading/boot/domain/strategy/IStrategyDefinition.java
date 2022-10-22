package com.mg.trading.boot.domain.strategy;

import com.mg.trading.boot.domain.models.TickerQuote;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;

import java.util.List;

public interface IStrategyDefinition {
    String getSymbol();

    IParameters getParams();

    BarSeries getSeries();

    BarSeries updateSeries(List<TickerQuote> quotes);

    Strategy getStrategy();

    Rule getEntryRule();

    Rule getExitRule();
}
