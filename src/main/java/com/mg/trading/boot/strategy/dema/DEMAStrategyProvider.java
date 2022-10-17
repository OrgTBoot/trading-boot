package com.mg.trading.boot.strategy.dema;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.AfterMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendGreenIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendSellIndicator;
import com.mg.trading.boot.strategy.rules.TotalLossToleranceRule;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyProvider implements StrategyProvider {

    private final DEMAParameters params;
    private Strategy strategy;

    public DEMAStrategyProvider(DEMAParameters strategyContext) {
        this.params = strategyContext;
    }

    public DEMAStrategyProvider(String symbol, BigDecimal sharesQty) {
        this.params = DEMAParameters.optimal(symbol, sharesQty);
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyParameters getParameters() {
        return this.params;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());

        //ENTRY RULES
        Rule crossedUp = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule sessionLossTolerance = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());

        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
        Rule enterRule = crossedUp.and(marketHours).and(sessionLossTolerance);

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bandFacade.upper());
        Rule stopLoss = new StopLossRule(closePrice, params.getPositionStopLossPercent());
        Rule stopGain = new StopGainRule(closePrice, params.getPositionStopGainPercent());
        Rule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
        Rule hasProfit = new StopGainRule(closePrice, 0.3);
        Rule sell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));
        Rule notGreenTrend = new BooleanIndicatorRule(new SuperTrendGreenIndicator(series, params.getShortBarCount())).negation();

        Rule exitRule =
                bollingerCrossUp
                .or(stopGain.and(notGreenTrend))
                .or(afterMarketHours.and(hasProfit))
                .or(sell)
                .or(stopLoss);

        String strategyName = "DEMA" + "_" + params.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
