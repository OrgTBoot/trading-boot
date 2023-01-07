package com.mg.trading.boot.domain.strategy.etf2;

import com.mg.trading.boot.domain.indicators.supertrentv2.AwesomeIndicatorWithTrend;
import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import com.mg.trading.boot.domain.strategy.etf1.ETFParametersV1;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;
import static com.mg.trading.boot.domain.strategy.TradingStrategies.ETF_V2;

@Log4j2
public class ETFStrategyV2 extends AbstractStrategyDefinition {

    private final ETFParametersV2 params = ETFParametersV2.optimal();
    private Strategy strategy;

    public ETFStrategyV2(String symbol) {
        super(symbol, ETF_V2.name());
    }

    @Override
    public ETFParametersV2 getParams() {
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

        SuperTrend superTrend10_3 = new SuperTrend(series, 10, 3D);
        SuperTrend superTrend20_6 = new SuperTrend(series, 20, 6D);
        SuperTrend superTrend20_8 = new SuperTrend(series, 20, 8D);
        //ENTRY RULES
        Rule crossedUpDEMA = debug(new CrossedUpIndicatorRule(shortIndicator, longIndicator));


        Rule st1Buy = debug(new UnderIndicatorRule(superTrend10_3, closePrice), "ST1 BUY");
        Rule st2Buy = debug(new UnderIndicatorRule(superTrend20_6, closePrice), "ST2 BUY");
        Rule st3Buy = debug(new UnderIndicatorRule(superTrend20_8, closePrice), "ST2 BUY");
        Rule superTrendUp = debug(st1Buy.and(st2Buy).and(st3Buy), "All BUY");

        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");

        Rule stopTotalPercentLoss = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)));

        Rule entryRule = debug(superTrendUp
                        .and(marketHours)                         // and enter only in marked hours
                        .and(stopTotalPercentLoss.negation()),  // and avoid entering again in a bearish stock
                Type.ENTRY);

        //EXIT RULES
        Rule st1Sell = debug(new OverIndicatorRule(superTrend10_3, closePrice), "ST1 SELL");
        Rule st2Sell = debug(new OverIndicatorRule(superTrend20_6, closePrice), "ST2 SELL");
        Rule st3Sell = debug(new OverIndicatorRule(superTrend20_8, closePrice), "ST3 SELL");
        Rule superTrendDown = debug(st1Sell.and(st2Sell), "All SELL");

        Rule priceReachedBollingerUpper = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule priceUnderLongDEMA = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA over price");
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "Gain > 1%");
        Rule anyGain = debug(new StopGainRule(closePrice, 0.1), "Gain > 0.1%");
        Rule loss5Percent = debug(new StopLossRule(closePrice, 5), "Loss 5%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
        Rule trailingLoss = debug(new TrailingStopLossRule(closePrice, series.numOf(2)), "TRAILING LOSS SELL");
        Rule trailingLoss1 = debug(new TrailingStopLossRule(closePrice, series.numOf(1)), "TRAILING LOSS SELL");

        Rule exitRule = debug(
                superTrendDown
//                        .or(superTrendDown.and(trailingLoss1))
//                priceReachedBollingerUpper                                                   // indicates high probability of a trend reversal
//                        .or(superTrendDown.and(chandelierOverPrice.and(priceUnderLongDEMA))) // or downtrend and price under long double moving average
//                        .or(superTrendDown.and(priceUnderLongDEMA.and(loss5Percent)))        // or downtrend and lost 5 percent (ref: CVNA_loss_tolerance.json)
                        .or((market60MinLeft.and(gain1Percent)))           // or downtrend and 60m to market close, take profits >= 1%
                        .or(market30MinLeft.and(anyGain))                                    // or 30m to market close, take any profits > 0%
                        .or(market10MinLeft)                                                 // or 10m to market close, force close position even in loss
                        .or(stopTotalPercentLoss),                                           // or reached day max loss percent (ref: SYTA_loss_tolerance.json)
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }
//    private Strategy initStrategy() {
//        //INDICATORS
//        int BARS_200 = 200;
//        int BARS_60 = 60;
//        int BARS_12 = 12;
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        DoubleEMAIndicator medIndicator = new DoubleEMAIndicator(closePrice, BARS_60);
//        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, BARS_200);
//        SuperTrend superTrend = new SuperTrend(series, BARS_12, 3D);
//        DistanceFromMAIndicator distanceFromDEMAInd = new DistanceFromMAIndicator(series, longIndicator);
//
//        //SHARED RULES
//        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
//        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
//        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
//
//        //ENTRY RULES
//        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
//        Rule priceOverMedDEMABuy = debug(new OverIndicatorRule(closePrice, medIndicator));
//        Rule priceOverLongDEMABuy = debug(new OverIndicatorRule(closePrice, longIndicator));
//        Rule medOverLongDEMABuy = debug(new OverIndicatorRule(medIndicator, longIndicator));
//
//        Rule superTrendBuy = debug(new UnderIndicatorRule(superTrend, closePrice), "ST BUY");
//        Rule distanceDEMABuy = debug(new OverIndicatorRule(distanceFromDEMAInd, 0.005), "DEMA DIST BUY");
//
//
//        Rule entryRule = debug(marketHours
//                        .and(priceOverLongDEMABuy)
//                        .and(priceOverMedDEMABuy)
//                        .and(medOverLongDEMABuy)
//                        .and(distanceDEMABuy)
//                        .and(superTrendBuy),
//                Type.ENTRY);
//
//
//        //EXIT RULES
//        Rule superTrendSell = debug(new OverIndicatorRule(superTrend, closePrice), "ST SELL");
//        Rule trailingLoss = debug(new TrailingStopLossRule(closePrice, series.numOf(2)), "TRAILING LOSS SELL");
//
//
//        Rule exitRule = debug(superTrendSell
//                        .or(market10MinLeft),
//                Type.EXIT);
//
//        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
//    }


//    private Strategy initStrategy() {
//        //INDICATORS
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
//        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
//        ZLEMAIndicator zlemaIndicator = new ZLEMAIndicator(closePrice, params.getLongBarCount());
//        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);
//        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());
//
//        //SHARED RULES
//        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
//        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
//        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
//
//        //ENTRY RULES
//        Rule priceOverLongDEMABuy = debug(new OverIndicatorRule(closePrice, longIndicator));
//        Rule superTrendBuy = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP, 3D), "BUY");
//        Rule superTrendSlowBuy = debug(new UnderIndicatorRule(new SuperTrend(series, 20, 4D), closePrice), "BUY SLOW");
//        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
//        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)));
//        Rule lastExit30BarsAgo = new IntervalFromLastTradeRule(30, Trade.TradeType.SELL);
//        Rule oneTransaction = new PositionsCountRule(1);
//
//        DistanceFromMAIndicator distanceFromLongDEMAInd = new DistanceFromMAIndicator(series, longIndicator);
//        Rule distanceDemaBuy = debug(new OverIndicatorRule(distanceFromLongDEMAInd, 0.005), "DEMA DIST BUY");
//
//
//        Rule entryRule = debug(marketHours
//                        .and(priceOverLongDEMABuy)
//                        .and(distanceDemaBuy)
//                        .and(superTrendBuy)
//                        .and(superTrendSlowBuy)
//                        .and(lastExit30BarsAgo)
//                        .and(stopTotalLossRule.negation()),
//                Type.ENTRY);
//
//
//        //EXIT RULES
//        Rule priceUnderDEMASell = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA cross Down");
//        Rule crossedDownDEMASell = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator), "DEMA cross Down");
//        Rule chandelierOverPriceSell = debug(new OverIndicatorRule(chandLong, closePrice));
//        Rule zlemaOverPriceSell = debug(new OverIndicatorRule(zlemaIndicator, closePrice));
//
//        Rule loss2Percent = debug(new StopLossRule(closePrice, 2), "LOSS 2%");
//        Rule loss05Percent = debug(new StopLossRule(closePrice, 0.5), "LOSS 2%");
//        Rule gain3Percent = debug(new StopGainRule(closePrice, 3), "GAIN 3%");
//        Rule gain05Percent = debug(new StopGainRule(closePrice, 0.5), "GAIN 0.5%");
//        Rule gainAny = debug(new StopGainRule(closePrice, 0.01), "GAIN 0.1%");
//        Rule downTrendWith3PercentGain = debug(zlemaOverPriceSell.and(gain3Percent), "DT 3% GAIN");
//        Rule downTrendWith05Gain = debug(zlemaOverPriceSell.and(gain05Percent), "DT 0.5% GAIN");
//        Rule downTrendLoss05 = debug(loss05Percent.and(crossedDownDEMASell).and(chandelierOverPriceSell), "DT 0.5% LOSS");
//        Rule trailingLoss = debug(new TrailingStopLossRule(closePrice, series.numOf(2.5)), "TRAILING LOSS SELL");
//
//
//        Rule exitRule = debug(
////                spikeSell
////                        .or(downTrendWith3PercentGain)
//                trailingLoss
////                        .or(downTrendWith05Gain)
////                        .or(downTrendLoss05)
////                        .or(loss2Percent.and(zlemaOverPriceSell))
////                        .or(downTrend)
////                        .or(market60MinLeft.and(gainAny)) // todo: P2: once we have more data sets test with this param, might be more profitable
////                        .or(market60MinLeft.and(gain05Percent))
//                        .or(market30MinLeft.and(gainAny))
//                        .or(market10MinLeft)
//                        .or(stopTotalLossRule),
//                Type.EXIT);
//
//        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
//    }

}
