package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.indicators.AfterMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
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
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross</a>
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

        //ENTER RULES
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, parameters.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, parameters.getLongBarCount());
        CrossedUpIndicatorRule crossedUp = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        BooleanIndicatorRule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series)); //enter only during market hours
        Rule enterRule = crossedUp.and(marketHours);

        //EXIT RULES
        StopLossRule stopLoss = new StopLossRule(closePrice, parameters.getStopLossPercent());
        StopGainRule stopGain = new StopGainRule(closePrice, parameters.getStopGainPercent());
        BooleanIndicatorRule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
        StopGainRule hasProfit = new StopGainRule(closePrice, 0.3);

        Rule exitRule = stopLoss.or(stopGain).or(afterMarketHours.and(hasProfit));

        String strategyName = getClass().getSimpleName() + "_" + parameters.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
