package com.mg.trading.boot.domain.strategy.etf3;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DistanceFromMAIndicator;
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
public class ETFStrategyV3 extends AbstractStrategyDefinition {

    private final ETFParametersV3 params = ETFParametersV3.optimal();
    private Strategy strategy;

    public ETFStrategyV3(String symbol) {
        super(symbol, "ETFV3");
    }

    @Override
    public ETFParametersV3 getParams() {
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
        int stLength1 = params.getShortBarCount();
        int stLength2 = params.getShortBarCount() + 1;
        int stLength3 = params.getShortBarCount() + 3;

        Rule priceOverLongDEMA = debug(new OverIndicatorRule(closePrice, longIndicator));
        Rule crossDEMAs = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));
        Rule superTrendUp1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.UP, 1D), "BUY1");
        Rule superTrendUp2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.UP, 2D), "BUY2");
        Rule superTrendUp3 = debug(new SuperTrendTrendRule(series, stLength3, Trend.UP, 3D), "BUY3");
        Rule superTrendUp = debug(superTrendUp1.and(superTrendUp2).and(superTrendUp3), "All BUY");

        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule onePosition = new PositionsCountRule(1);
        Rule distanceFromLastEntry = new IntervalFromLastTradeRule(params.getShortBarCount(), Trade.TradeType.SELL);
        Rule chandelierUnderPrice = debug(new UnderIndicatorRule(chandLong, closePrice));

        Rule entryRule = debug(marketHours
                        .and(priceOverLongDEMA)
//                        .and(chandelierUnderPrice)
                        .and(superTrendUp)
//                        .and(distanceFromLastEntry)
                        .and(onePosition.negation())
                        .and(stopTotalLossRule.negation()),
                Type.ENTRY);

        //EXIT RULES
        Rule superTrendDown1 = debug(new SuperTrendTrendRule(series, stLength1, Trend.DOWN, 1D), "SELL1");
        Rule superTrendDown2 = debug(new SuperTrendTrendRule(series, stLength2, Trend.DOWN, 2D), "SELL2");
        Rule superTrendDown3 = debug(new SuperTrendTrendRule(series, stLength3, Trend.DOWN, 3D), "SELL3");
        Rule superTrendDown = debug(superTrendDown1.and(superTrendDown2).and(superTrendDown3), "All SELL");
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule priceUnderLongDEMA = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA over price");
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "Gain > 1%");
        Rule anyGain = debug(new StopGainRule(closePrice, 0.1), "Gain > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
        Rule trailingLoss1 = debug(new TrailingStopLossRule(closePrice, series.numOf(1)), "TRAILING LOSS SELL");

        Rule exitRule = debug(
                stopTotalLossRule
                        .or(bollingerCrossUp.and(anyGain))
                        .or(gain1Percent.and(trailingLoss1))
                        .or(superTrendDown.and(chandelierOverPrice).and(priceUnderLongDEMA))
                        .or(superTrendDown.and(market60MinLeft).and(gain1Percent))
                        .or(market30MinLeft.and(anyGain))
                        .or(market10MinLeft),
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }


}
