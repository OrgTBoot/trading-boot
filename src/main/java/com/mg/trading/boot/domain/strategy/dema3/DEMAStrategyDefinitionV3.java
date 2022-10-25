package com.mg.trading.boot.domain.strategy.dema3;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
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

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.AFTER_HOURS;
import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV3 extends AbstractStrategyDefinition {

    private final DEMAParametersV3 params = DEMAParametersV3.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV3(String symbol) {
        super(symbol, "DEMAV3");
    }

    @Override
    public DEMAParametersV3 getParams() {
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

        //ENTRY RULES
        Rule marketHours = new MarketHoursRule(series).or(new MarketPreHoursRule(series));
        Rule crossedUpDEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule stopTotalLossRule = new StopTotalLossRule(series, params.getTotalLossThresholdPercent());
        Rule superTrendUpSignalUp = new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP);
        Rule market60MinLeft = new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES);

//        Rule superTrendUpSignalNone = new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.NO_SIGNAL);

        Rule entryRule = crossedUpDEMA                    // 1. trend
                .and(superTrendUpSignalUp)                // 2. and confirmation
                .and(marketHours)                         // 3. and enter only in marked hours
                .and(stopTotalLossRule.negation())        // 4. and avoid entering again in a bearish stock
                .and(market60MinLeft.negation());         // 5. and avoid entering in 60 min before market close

        //EXIT RULES
        Rule superTrendSell = new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN);
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bollinger.upper()).and(superTrendSell);
        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator).and(superTrendSell);

        Rule has1PercentProfit = new StopGainRule(closePrice, 1);
        Rule hasAnyProfit = new StopGainRule(closePrice, 0.1);
        Rule market30MinLeft = new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES);
        Rule market10MinLeft = new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES);

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(market60MinLeft.and(has1PercentProfit))   // 3. or 60m to market close, take profits >= 1%
                .or(market30MinLeft.and(hasAnyProfit))        // 4. or 30m to market close, take any profits > 0%
                .or(market10MinLeft)                          // 5. or 10m to market close, force close position even in loss
                .or(stopTotalLossRule);                       // 6. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
