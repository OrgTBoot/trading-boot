package com.mg.trading.boot.domain.strategy.dema8;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
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
 * For more details see: <a href="https://www.youtube.com/watch?v=u1b5v2Is8iY&feature=youtu.be">Supper Trend</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV8 extends AbstractStrategyDefinition {

    private final DEMAParametersV8 params = DEMAParametersV8.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV8(String symbol) {
        super(symbol, "DEMAV8");
    }

    @Override
    public DEMAParametersV8 getParams() {
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
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);

        //ENTRY RULES
        Rule crossedUpDEMA = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        int stLength1 = params.getShortBarCount();
        int stLength2 = params.getShortBarCount() * 2;
        Double stMultiplier1 = 3D;
        Double stMultiplier2 = 6D;

        Rule superTrendUp1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.UP, stMultiplier1), "BUY1");
        Rule superTrendUp2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.UP, stMultiplier2), "BUY2");
        Rule superTrendUp = debug(superTrendUp1.and(superTrendUp2), "All BUY");

        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");

        Rule stopTotalPercentLoss = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));

        Rule entryRule = debug(superTrendUp
                        .and(marketHours)                         // and enter only in marked hours
                        .and(stopTotalPercentLoss.negation()),  // and avoid entering again in a bearish stock
                Type.ENTRY);

        //EXIT RULES
        Rule superTrendDown1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.DOWN, stMultiplier1), "SELL1");
        Rule superTrendDown2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.DOWN, stMultiplier2), "SELL2");
        Rule superTrendDown = debug(superTrendDown1.and(superTrendDown2), "All SELL");
        Rule priceReachedBollingerUpper = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule priceUnderLongDEMA = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA over price");
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "Gain > 1%");
        Rule anyGain = debug(new StopGainRule(closePrice, 0.1), "Gain > 0.1%");
        Rule loss5Percent = debug(new StopLossRule(closePrice, 5), "Loss 5%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = debug(
                priceReachedBollingerUpper                                                   // indicates high probability of a trend reversal
                        .or(superTrendDown.and(chandelierOverPrice.and(priceUnderLongDEMA))) // or downtrend and price under long double moving average
                        .or(superTrendDown.and(priceUnderLongDEMA.and(loss5Percent)))        // or downtrend and lost 5 percent (ref: CVNA_loss_tolerance.json)
                        .or(superTrendDown.and(market60MinLeft.and(gain1Percent)))           // or downtrend and 60m to market close, take profits >= 1%
                        .or(market30MinLeft.and(anyGain))                                    // or 30m to market close, take any profits > 0%
                        .or(market10MinLeft)                                                 // or 10m to market close, force close position even in loss
                        .or(stopTotalPercentLoss),                                           // or reached day max loss percent (ref: SYTA_loss_tolerance.json)
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
