package com.mg.trading.boot.domain.strategy.dema2;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
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
import org.ta4j.core.rules.*;

import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV2 extends AbstractStrategyDefinition {

    private final DEMAParametersV2 params = DEMAParametersV2.optimal();
    private Strategy strategy;

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

    private Strategy initStrategy() {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);

        //ENTRY RULES
        Rule marketHours = debug(new MarketHoursRule(series));
        Rule crossedUpDEMA = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        Rule chandelierUnderPrice = debug(new UnderIndicatorRule(chandLong, closePrice));
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()), "NotStopLoss");
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");

        Rule entryRule = debug(
                crossedUpDEMA                                     // 1. trend
                        .and(chandelierUnderPrice)                // 2. and confirmation
                        .and(marketHours)                         // 3. and enter only in marked hours
                        .and(market60MinLeft.negation())          // 4. and avoid entering in 60 min before market close
                        .and(stopTotalLossRule.negation()),       // 5. and avoid entering again in a bearish stock
                Type.ENTRY);

        //EXIT RULES
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()));
        Rule crossedDownDEMA = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator));
        Rule superTrendSell = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN));

        Rule has1PercentProfit = debug(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyProfit = debug(new StopGainRule(closePrice, 0.1), "Has > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = debug(
                bollingerCrossUp                                      // 1. trend reversal signal, reached upper line, market will start selling
                        .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                        .or(market60MinLeft.and(has1PercentProfit))   // 3. or 60m to market close, take profits >= 1%
                        .or(market30MinLeft.and(hasAnyProfit))        // 4. or 30m to market close, take any profits > 0%
                        .or(market10MinLeft)                          // 5. or 10m to market close, force close position even in loss
                        .or(stopTotalLossRule), Type.EXIT);           // 6. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
