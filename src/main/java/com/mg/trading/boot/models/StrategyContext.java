package com.mg.trading.boot.models;

import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.strategy.core.StrategyParameters;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

@Getter
@Builder
@ToString
public class StrategyContext {
    private BrokerProvider broker;
    private TickerQuoteProvider quoteProvider;
//    private String symbol;
    private BarSeries series;
    private Strategy strategy;
    private StrategyParameters parameters;
}
