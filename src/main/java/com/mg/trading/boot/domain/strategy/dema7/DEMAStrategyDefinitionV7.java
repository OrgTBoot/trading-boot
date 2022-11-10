package com.mg.trading.boot.domain.strategy.dema7;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema5.DEMAParametersV5;
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


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV7 extends AbstractStrategyDefinition {

    private final DEMAParametersV7 params = DEMAParametersV7.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV7(String symbol) {
        super(symbol, "DEMAV7");
    }

    @Override
    public DEMAParametersV7 getParams() {
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
        int stLength3 = params.getShortBarCount() + 2;

        Rule priceOverLongDEMA = trace(new OverIndicatorRule(closePrice, longIndicator));
        Rule superTrendUp1 = trace(new SuperTrendTrendRule(series, stLength1, Trend.UP, 1));
        Rule superTrendUp2 = trace(new SuperTrendTrendRule(series, stLength2, Trend.UP, 2));
        Rule superTrendUp3 = trace(new SuperTrendTrendRule(series, stLength3, Trend.UP, 3));

        Rule marketHours = trace(new MarketHoursRule(series).or(new MarketPreHoursRule(series)));
        Rule market60MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule stopTotalLossRule = trace(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));


        Rule entryRule = trace(priceOverLongDEMA.and(superTrendUp1).and(superTrendUp2).and(superTrendUp3).and(marketHours)                         // 3. and enter only in marked hours
                        .and(market60MinLeft.negation())          // 4. and avoid entering in 60 min before market close
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

//        Rule has2PercentProfit = trace(new StopGainRule(closePrice, 2), "Has > 2%");
        Rule has1PercentProfit = trace(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyProfit = trace(new StopGainRule(closePrice, 0.1), "Has > 0.1%");
        Rule market30MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = trace(
                crossedDownDEMA.and(superTrendDown).and(chandelierOverPrice)
//                        .or(has2PercentProfit)
                        .or(bollingerCrossUp)
                        .or(market60MinLeft.and(has1PercentProfit))   // 4. or 60m to market close, take profits >= 1%
                        .or(market30MinLeft.and(hasAnyProfit))        // 5. or 30m to market close, take any profits > 0%
                        .or(market10MinLeft)                          // 6. or 10m to market close, force close position even in loss
                        .or(stopTotalLossRule),                       // 7. or reached day max loss percent for a given symbol
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
