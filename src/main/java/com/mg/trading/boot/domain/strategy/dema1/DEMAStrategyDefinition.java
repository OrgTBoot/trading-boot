package com.mg.trading.boot.domain.strategy.dema1;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
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

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


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
    public void setSeries(BarSeries series) {
        this.series = series;
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
        Rule crossedUp = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");

        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
        Rule entryRule = debug(crossedUp
                .and(marketHours)
                .and(stopTotalLossRule.negation()), Type.ENTRY);

        //EXIT RULES
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bandFacade.upper()));
        Rule crossedDownDEMA = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator));
        Rule superTrendSell = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN));

        Rule has1PercentProfit = debug(new StopGainRule(closePrice, 1));
        Rule hasAnyProfit = debug(new StopGainRule(closePrice, 0.1));
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES));
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES));

        Rule exitRule = debug(bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(market60MinLeft.and(has1PercentProfit))   // 3. or 60m to market close, take profits >= 1%
                .or(market30MinLeft.and(hasAnyProfit))        // 4. or 30m to market close, take any profits > 0%
                .or(market10MinLeft)                          // 5. or 10m to market close, force close position even in loss
                .or(stopTotalLossRule), Type.EXIT);           // 6. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
