package com.mg.trading.boot.strategy.goldencross;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitShortIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross</a>
 */
@Log4j2
public class SuperTrendDEMAStrategyProvider implements StrategyProvider {

    private final DEMAParameters parameters;
    private Strategy strategy;

    public SuperTrendDEMAStrategyProvider(DEMAParameters parameters) {
        this.parameters = parameters;
    }

    public SuperTrendDEMAStrategyProvider(String symbol) {
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
//        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, parameters.getShortBarCount());
        DoubleEMAIndicator dema = new DoubleEMAIndicator(closePrice, parameters.getLongBarCount());
//        CrossedUpIndicatorRule crossedDEMA = new CrossedUpIndicatorRule(closePrice, longIndicator);
        OverIndicatorRule overDEMA = new OverIndicatorRule(dema, closePrice);
//        BooleanIndicatorRule buy = new BooleanIndicatorRule(new SuperTrendSignalIndicator(series, 10, OrderAction.BUY));
//        BooleanIndicatorRule sell = new BooleanIndicatorRule(new SuperTrendSignalIndicator(series, 10,
//                OrderAction.SELL));
        //enter
        // only during market hours
        Rule enterRule = overDEMA;//.and(buy);

        //EXIT RULES
//        StopLossRule stopLoss = new StopLossRule(closePrice, parameters.getStopLossPercent());
//        StopGainRule stopGain = new StopGainRule(closePrice, parameters.getStopGainPercent());
//        BooleanIndicatorRule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
//        StopGainRule hasProfit = new StopGainRule(closePrice, 0.3);
//        BooleanIndicatorRule sell = new BooleanIndicatorRule(new SuperTrendSignalIndicator(series, 10, OrderAction.SELL));
        ChandelierExitShortIndicator chandelierExit = new ChandelierExitShortIndicator(series);
//        StopLossRule stopLoss = new StopLossRule(chandelierExit, parameters.getStopLossPercent());

        UnderIndicatorRule underDMA = new UnderIndicatorRule(dema, closePrice);
        Rule exitRule = underDMA;

        String strategyName = getClass().getSimpleName() + "_" + parameters.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
