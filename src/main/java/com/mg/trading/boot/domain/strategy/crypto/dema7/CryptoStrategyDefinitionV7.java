package com.mg.trading.boot.domain.strategy.crypto.dema7;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema7.DEMAParametersV7;
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

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


@Log4j2
public class CryptoStrategyDefinitionV7 extends AbstractStrategyDefinition {

    private final CryptoParametersV7 params = CryptoParametersV7.optimal();
    private Strategy strategy;

    public CryptoStrategyDefinitionV7(String symbol) {
        super(symbol, "CRYPTO_V7");
    }

    @Override
    public CryptoParametersV7 getParams() {
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
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);

        //ENTRY RULES
        Rule crossedUpDEMA = trace(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        int stLength1 = params.getShortBarCount();
        int stLength2 = params.getShortBarCount() + 1;
        int stLength3 = params.getShortBarCount() + 3;

        Rule priceOverLongDEMA = trace(new OverIndicatorRule(closePrice, longIndicator));
        Rule superTrendUp1 = trace(new SuperTrendTrendRule(series, stLength1, Trend.UP, 1));
        Rule superTrendUp2 = trace(new SuperTrendTrendRule(series, stLength2, Trend.UP, 2));
        Rule superTrendUp3 = trace(new SuperTrendTrendRule(series, stLength3, Trend.UP, 3));

        Rule stopTotalLossRule = trace(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));


        Rule entryRule = trace(priceOverLongDEMA
                        .and(superTrendUp1)
                        .and(superTrendUp2)
                        .and(superTrendUp3)
                        .and(stopTotalLossRule.negation()),       // 5. and avoid entering again in a bearish stock
                Type.ENTRY);


        //EXIT RULES
        Rule bollingerCrossUp = trace(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule crossedDownDEMA = trace(new CrossedDownIndicatorRule(shortIndicator, longIndicator), "DEMA cross Down");
        Rule superTrendDown1 = trace(new SuperTrendTrendRule(series, stLength1, Trend.DOWN, 1), "SELL1");
        Rule superTrendDown2 = trace(new SuperTrendTrendRule(series, stLength2, Trend.DOWN, 2), "SELL2");
        Rule superTrendDown3 = trace(new SuperTrendTrendRule(series, stLength3, Trend.DOWN, 3), "SELL3");
        Rule superTrendDown = superTrendDown1.and(superTrendDown2).and(superTrendDown3);

        Rule chandelierOverPrice = trace(new OverIndicatorRule(chandLong, closePrice));

        Rule gain1Percent = trace(new StopGainRule(closePrice, 1), "Gain > 1%");
        Rule loss2Percent = trace(new StopLossRule(closePrice, 2), "Loss -3%");

        Rule exitRule = trace(
                superTrendDown.and(gain1Percent.or(loss2Percent))
                        .or(bollingerCrossUp)
                        .or(stopTotalLossRule),                       // 7. or reached day max loss percent for a given symbol
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
