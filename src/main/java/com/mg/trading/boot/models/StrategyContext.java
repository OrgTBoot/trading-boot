package com.mg.trading.boot.models;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.TickerQuoteExtractor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

@Getter
@Builder
@ToString
public class StrategyContext {
    private BarSeries series;
    private TradingRecord tradingRecord;
    private Strategy strategy;
    private StrategyParameters parameters;
    private TickerQuoteExtractor quoteExtractor;
}
