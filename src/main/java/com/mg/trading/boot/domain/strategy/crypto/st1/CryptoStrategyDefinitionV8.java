package com.mg.trading.boot.domain.strategy.crypto.st1;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


@Log4j2
public class CryptoStrategyDefinitionV8 extends AbstractStrategyDefinition {

    private final CryptoParametersV8 params = CryptoParametersV8.optimal();
    private Strategy strategy;

    public CryptoStrategyDefinitionV8(String symbol) {
        super(symbol, "CRYPTO_V7");
    }

    @Override
    public CryptoParametersV8 getParams() {
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
        Rule crossedUpDEMA = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        int stLength1 = params.getShortBarCount();
        int stLength2 = params.getShortBarCount() * 2;

        Rule superTrendUp1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.UP, 3D), "BUY1");
        Rule superTrendUp2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.UP, 5D), "BUY2");
        Rule superTrendUp = debug(superTrendUp1.and(superTrendUp2), "All BUY");

        Rule marketHours = debug(new MarketHoursRule(series).or(new MarketPreHoursRule(series)));
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));

        Rule entryRule = debug(superTrendUp
                        .and(stopTotalLossRule.negation()),       // 5. and avoid entering again in a bearish stock
                Type.ENTRY);

        //EXIT RULES
        Rule superTrendDown1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.DOWN, 3D), "SELL1");
        Rule superTrendDown2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.DOWN, 5D), "SELL2");
        Rule superTrendDown = debug(superTrendDown1.and(superTrendDown2), "All SELL");
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule priceUnderLongDEMA = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA over price");
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "Gain > 1%");
        Rule anyGain = debug(new StopGainRule(closePrice, 0.1), "Gain > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = debug(bollingerCrossUp.and(anyGain)                                                // indicates high probability of a trend reversal
                        .or(superTrendDown.and(chandelierOverPrice).and(priceUnderLongDEMA)) // downtrend and price under long double moving average
                        .or(stopTotalLossRule),                                              // or reached day max loss percent for a given symbol
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }
}
