package com.mg.trading.boot.domain.strategy.crypto.demav4;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema4.DEMAParametersV4;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class CryptoDEMAStrategyDefinitionV5 extends AbstractStrategyDefinition {

    private final DEMAParametersV4 params = DEMAParametersV4.optimal();
    private Strategy strategy;

    public CryptoDEMAStrategyDefinitionV5(String symbol) {
        super(symbol, "CRYPTO_DEMAV5");
    }

    @Override
    public DEMAParametersV4 getParams() {
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
//        Rule crossedUpDEMA = trace(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        Rule priceOverLongDEMA = trace(new OverIndicatorRule(closePrice, longIndicator));
        Rule chandelierUnderPrice = trace(new UnderIndicatorRule(chandLong, closePrice));
        Rule stopTotalLossRule = trace(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule superTrendUpSignalUp = trace(new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP));

        Rule entryRule = trace(
                priceOverLongDEMA
                        .and(superTrendUpSignalUp)
                        .and(stopTotalLossRule.negation()),
                Type.ENTRY);


        //EXIT RULES
        Rule bollingerCrossUp = trace(new OverIndicatorRule(closePrice, bollinger.upper()));
        Rule crossedDownDEMA = trace(new CrossedDownIndicatorRule(shortIndicator, longIndicator));
        Rule superTrendSell = trace(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN));

        Rule has5PercentLoss = trace(new StopLossRule(closePrice, 5), "Has -5%");

        Rule priceCrossedDownDEMA = new CrossedDownIndicatorRule(closePrice, longIndicator);
        Rule exitRule = trace(
                bollingerCrossUp
                        .or(crossedDownDEMA.and(superTrendSell).and(chandelierUnderPrice.negation().or(priceCrossedDownDEMA)))
                        .or(has5PercentLoss.and(superTrendSell).and(chandelierUnderPrice.negation().or(priceCrossedDownDEMA)))
                        .or(stopTotalLossRule), Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
