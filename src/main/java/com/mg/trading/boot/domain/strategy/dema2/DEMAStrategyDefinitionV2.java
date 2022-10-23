package com.mg.trading.boot.domain.strategy.dema2;

import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV2 extends AbstractStrategyDefinition {

    private final DEMAParametersV2 params = DEMAParametersV2.optimal();
    private Strategy strategy;
    private Rule entryRule;
    private Rule exitRule;

    public DEMAStrategyDefinitionV2(String symbol) {
        super(symbol, "DEMAV2");
    }

    @Override
    public DEMAParametersV2 getParams() {
        return params;
    }

    @Override
    public Strategy getStrategy() {
        if (strategy == null) {
            this.strategy = initStrategy();
        }
        return strategy;
    }

    @Override
    public Rule getEntryRule() {
        return entryRule;
    }

    @Override
    public Rule getExitRule() {
        return exitRule;
    }

    private Strategy initStrategy() {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);

        //ENTRY RULES
        Rule preMarketHours = new MarketPreHoursRule(series);
        Rule marketHours = new MarketHoursRule(series);
        Rule crossedUpDEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule chandelierUnderPrice = new UnderIndicatorRule(chandLong, closePrice);
        Rule stopTotalLossRule = new StopTotalLossRule(series, params.getTotalLossThresholdPercent());

        this.entryRule = crossedUpDEMA                    // 1. trend
                .and(chandelierUnderPrice)                // 2. and confirmation
                .and(stopTotalLossRule.negation())        // 3. and avoid entering again in a bearish stock
                .and(preMarketHours.or(marketHours));     // 4. and enter only in marked hours

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bollinger.upper());
        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator);
        Rule superTrendSell = new SuperTrendSellRule(series, params.getShortBarCount());
        Rule extendedMarketHours = new MarketExtendedHoursRule(series);
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
//        Rule stopLossRule = new XStopLossRule(closePrice, params.getTotalLossThresholdPercent());
        Rule timeToExtendedHoursClose = new MarketTimeToExtendedHoursCloseRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        this.exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(timeToExtendedHoursClose)                 // 4. or last resort rule - dump position of approaching market close
                .or(stopTotalLossRule);                       // 5. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
