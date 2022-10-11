package com.mg.trading.boot.strategy.dema;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.*;
import com.mg.trading.boot.strategy.rules.TotalLossToleranceRule;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 */
@Log4j2
public class DEMAStrategyProvider implements StrategyProvider {

    private final DEMAParameters parameters;
    private Strategy strategy;

    public DEMAStrategyProvider(DEMAParameters strategyContext) {
        this.parameters = strategyContext;
    }

    public DEMAStrategyProvider(String symbol) {
        this.parameters = DEMAParameters.optimal(symbol);
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyParameters getParameters() {
        return this.parameters;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, parameters.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, parameters.getLongBarCount());


        //ENTER RULES
        Rule crossedUp = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule sessionLossTolerance = new TotalLossToleranceRule(series, 3);

        Rule enterRule = crossedUp.and(marketHours).and(sessionLossTolerance);

        //EXIT RULES
        Rule stopLoss = new StopLossRule(closePrice, parameters.getPositionStopLossPercent());
        Rule stopGain = new StopGainRule(closePrice, parameters.getPositionStopGainPercent());
        Rule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
        Rule hasProfit = new StopGainRule(closePrice, 0.3);
        Rule sell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, parameters.getShortBarCount()));
        Rule notGreenTrend = new BooleanIndicatorRule(new SuperTrendGreenIndicator(series, parameters.getShortBarCount())).negation();

        Rule exitRule = stopGain.and(notGreenTrend)
                .or(afterMarketHours.and(hasProfit))
                .or(sell)
                .or(stopLoss); // todo: test more with this criteria, run it on more stocks

        String strategyName = getClass().getSimpleName() + "_" + parameters.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
