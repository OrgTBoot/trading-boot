package com.mg.trading.boot.domain.strategy.dema2;

import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.strategy.XAbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class XDEMAStrategyDefinitionV2 extends XAbstractStrategyDefinition {

    private final XDEMAParametersV2 params = XDEMAParametersV2.optimal();
    private Strategy strategy;

    public XDEMAStrategyDefinitionV2(String symbol) {
        super(symbol, "DEMAV2");
    }

    @Override
    public XDEMAParametersV2 getParams() {
        return params;
    }

    @Override
    public Strategy getStrategy() {
        if (strategy == null) {
            this.strategy = initStrategy();
        }
        return strategy;
    }

    private Strategy initStrategy() {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);

        //ENTRY RULES
        Rule preMarketHours = new XPreMarketHoursRule(series);
        Rule marketHours = new XMarketHoursRule(series);
        Rule crossedUpDEMA = new XCrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule chandelierUnderPrice = new XUnderIndicatorRule(chandLong, closePrice);
        Rule dayMaxLossNotReached = new XUnderTotalLossThresholdRule(series, params.getTotalLossThresholdPercent());

        Rule entryRule = crossedUpDEMA      // 1. trend
                .and(chandelierUnderPrice)           // 2. and confirmation
                .and(dayMaxLossNotReached)           // 3. and avoid entering again in a bearish stock
                .and(preMarketHours.or(marketHours));// 4. and enter only in marked hours

        //EXIT RULES
        Rule bollingerCrossUp = new XOverIndicatorRule(closePrice, bollinger.upper());
        Rule crossedDownDEMA = new XCrossedDownIndicatorRule(shortIndicator, longIndicator);
        Rule superTrendSell = new XSuperTrendSellRule(series, params.getShortBarCount());
        Rule extendedMarketHours = new XExtendedMarketHoursRule(series);
        Rule hasMinimalProfit = new XStopGainRule(closePrice, 0.1);
        Rule stopPositionLoss = new XStopLossRule(closePrice, params.getTotalLossThresholdPercent());
        Rule timeToExtendedHoursClose = new XTimeToMarketExtendedHoursRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);
        Rule reachedMaxAllowedLoss = dayMaxLossNotReached.negation();

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(timeToExtendedHoursClose)                 // 4. or last resort rule - dump position of approaching market close
                .or(reachedMaxAllowedLoss);                   // 5. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
