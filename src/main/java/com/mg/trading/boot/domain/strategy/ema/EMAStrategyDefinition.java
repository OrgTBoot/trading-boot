package com.mg.trading.boot.domain.strategy.ema;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
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
public class EMAStrategyDefinition extends AbstractStrategyDefinition {

    private final EMAParameters params = EMAParameters.optimal();
    private Strategy strategy;

    public EMAStrategyDefinition(String symbol) {
        super(symbol, "EMA");
    }

    @Override
    public EMAParameters getParams() {
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
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortIndicator = new EMAIndicator(closePrice, params.getShortBarCount());
        EMAIndicator longIndicator = new EMAIndicator(closePrice, params.getLongBarCount());

        //entry
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        Rule crossedUpEMA = trace(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        Rule marketHours = trace(new MarketHoursRule(series));
        Rule stopTotalLossRule = trace(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule market60MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES));

        Rule entryRule = trace(crossedUpEMA
                .and(marketHours)                         // and enter only in marked hours
                .and(stopTotalLossRule.negation())        // and avoid entering again in a bearish stock
                .and(market60MinLeft.negation()), Type.ENTRY);         // and avoid entering in 60 min before market close

        //exit
        Rule bollingerCrossUp = trace(new OverIndicatorRule(closePrice, bollinger.upper()));
        Rule crossedDownDEMA = trace(new CrossedDownIndicatorRule(shortIndicator, longIndicator));
        Rule superTrendSell = trace(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN));
        Rule has1PercentProfit = trace(new StopGainRule(closePrice, 1));
        Rule hasAnyProfit = trace(new StopGainRule(closePrice, 0.1));
        Rule market30MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES));
        Rule market10MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES));

        Rule exitRule = trace(bollingerCrossUp                // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(market60MinLeft.and(has1PercentProfit))   // 3. or 60m to market close, take profits >= 1%
                .or(market30MinLeft.and(hasAnyProfit))        // 4. or 30m to market close, take any profits > 0%
                .or(market10MinLeft)                          // 5. or 10m to market close, force close position even in loss
                .or(stopTotalLossRule), Type.EXIT);           // 6. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
