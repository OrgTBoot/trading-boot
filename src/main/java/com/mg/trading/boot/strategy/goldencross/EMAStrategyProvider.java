package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.strategy.indicators.AfterMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;


/**
 * Apply two moving averages to a chart: one longer and one shorter.
 * When the shorter-term MA crosses above the longer-term MA, it's a buy signal, as it indicates that the trend is
 * shifting up. This is known as a golden cross. Meanwhile, when the shorter-term EMA crosses below the
 * longer-term EMA, it's a sell signal, as it indicates that the trend is shifting down
 * <p>
 * For more details see: <a href="https://www.youtube.com/watch?v=6mckJdktXkc">Golden Cross</a>
 */
@Log4j2
public class EMAStrategyProvider implements StrategyProvider {

    private final EMAParameters parameters;
    private Strategy strategy;

    public EMAStrategyProvider(EMAParameters parameters) {
        this.parameters = parameters;
    }

    public EMAStrategyProvider(String symbol) {
        this.parameters = EMAParameters.optimal(symbol);
    }

    @Override
    public EMAParameters getParameters() {
        return this.parameters;
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortIndicator = new EMAIndicator(closePrice, parameters.getShortBarCount());
        EMAIndicator longIndicator = new EMAIndicator(closePrice, parameters.getLongBarCount());

        //enter rules
        CrossedUpIndicatorRule crossedUpEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        BooleanIndicatorRule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series)); //enter only during market hours
        Rule enterRule = crossedUpEMA.and(marketHours);

        //exit rules - stop loss/gain at X% OR if we are in after hours and position is positive
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
