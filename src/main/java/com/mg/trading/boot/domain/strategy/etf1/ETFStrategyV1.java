package com.mg.trading.boot.domain.strategy.etf1;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;
import static com.mg.trading.boot.domain.strategy.TradingStrategies.DEMA_V6_1;

@Log4j2
public class ETFStrategyV1 extends AbstractStrategyDefinition {

    private final ETFParametersV1 params = ETFParametersV1.optimal();
    private Strategy strategy;

    public ETFStrategyV1(String symbol) {
        super(symbol, DEMA_V6_1.name());
    }

    @Override
    public ETFParametersV1 getParams() {
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
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());

        //SHARED RULES
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");

        //ENTRY RULES
        Rule priceOverLongDEMABuy = debug(new OverIndicatorRule(closePrice, longIndicator));
        Rule superTrendBuy = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP, 3D), "BUY");
        Rule superTrendSlowBuy = debug(new UnderIndicatorRule(new SuperTrend(series, 20, 4D), closePrice), "BUY SLOW");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)));
        Rule lastExit60BarsAgo = new IntervalFromLastTradeRule(30, Trade.TradeType.SELL);
        Rule oneTransaction = new PositionsCountRule(1);

        Rule entryRule = debug(marketHours
                        .and(priceOverLongDEMABuy)
                        .and(superTrendBuy)
                        .and(superTrendSlowBuy)
                        .and(lastExit60BarsAgo)
                        .and(stopTotalLossRule.negation()),
                Type.ENTRY);


        //EXIT RULES
        Rule priceUnderDEMASell = debug(new UnderIndicatorRule(closePrice, longIndicator), "DEMA cross Down");
        Rule crossedDownDEMASell = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator), "DEMA cross Down");
        Rule superTrendSell = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN, 3D), "SELL");
        Rule superTrendSlowSell = debug(new OverIndicatorRule(new SuperTrend(series, 20, 4D), closePrice), "SELL SLOW");
        Rule chandelierOverPriceSell = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule lastEntry5BarsAgo = debug(new IntervalFromLastTradeRule(5, Trade.TradeType.BUY), "ENTRY 5 BARS AGO");
        Rule bollingerSell = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "BOLLINGER CROSS UP");
        Rule spikeSell = debug(bollingerSell.and(lastEntry5BarsAgo), "SPIKE SELL");


        Rule loss2Percent = debug(new StopLossRule(closePrice, 2), "LOSS 2%");
        Rule loss05Percent = debug(new StopLossRule(closePrice, 0.5), "LOSS 2%");
        Rule gain3Percent = debug(new StopGainRule(closePrice, 3), "GAIN 3%");
        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "GAIN 1%");
        Rule gain05Percent = debug(new StopGainRule(closePrice, 0.5), "GAIN 0.5%");
        Rule gainAny = debug(new StopGainRule(closePrice, 0.01), "GAIN 0.1%");

        Rule downTrendWith3PercentGain = priceUnderDEMASell.and(gain3Percent);
        Rule downTrendWith05Gain = crossedDownDEMASell.and(gain05Percent);
        Rule downTrendWithGain = crossedDownDEMASell.and(superTrendSlowSell).and(gainAny);
        Rule downTrendLoss05 = loss05Percent.and(crossedDownDEMASell).and(chandelierOverPriceSell);
//        Rule downTrend = crossedDownDEMASell.and(superTrendSell).and(chandelierOverPriceSell);
        Rule downTrend = priceUnderDEMASell.and(superTrendSell).and(chandelierOverPriceSell);

        Rule exitRule = debug(spikeSell
                        .or(downTrendWith3PercentGain)
                        .or(downTrendWith05Gain)
                        .or(downTrendLoss05)
//                        .or(downTrend)
                        .or(loss2Percent.and(priceUnderDEMASell))
                        .or(market60MinLeft.and(gain1Percent))
                        .or(market30MinLeft.and(gainAny))
                        .or(market10MinLeft)
                        .or(stopTotalLossRule),
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
