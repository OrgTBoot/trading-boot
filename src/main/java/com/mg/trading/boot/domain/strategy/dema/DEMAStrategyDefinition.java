package com.mg.trading.boot.domain.strategy.dema;

import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;

import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinition extends AbstractStrategyDefinition {

    private final DEMAParameters params = DEMAParameters.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinition(String symbol) {
        super(symbol, "DEMA");
    }

    @Override
    public DEMAParameters getParams() {
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
        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());

        //ENTRY RULES
        Rule crossedUp = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule marketHours = new MarketHoursRule(series);
        Rule stopTotalLossRule = new StopTotalLossRule(series, params.getTotalLossThresholdPercent());

        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
        Rule entryRule = crossedUp
                .and(marketHours)
                .and(stopTotalLossRule.negation());

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bandFacade.upper());
        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator);
//        Rule superTrendSell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));
        Rule superTrendSell = new SuperTrendSellRule(series, params.getShortBarCount());
        Rule extendedMarketHours = new MarketExtendedHoursRule(series);
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new MarketTimeToExtendedHoursCloseRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(stopTotalLossRule)                        // 5. or reached day max loss percent for a given symbol
                .or(timeToMarketClose);                       // 6. or last resort rule - dump position of approaching market close

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
