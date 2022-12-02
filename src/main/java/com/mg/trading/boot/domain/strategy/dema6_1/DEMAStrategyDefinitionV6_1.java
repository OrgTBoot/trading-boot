package com.mg.trading.boot.domain.strategy.dema6_1;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import com.mg.trading.boot.domain.strategy.TradingStrategies;
import com.mg.trading.boot.domain.strategy.dema6.DEMAParametersV6;
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


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV6_1 extends AbstractStrategyDefinition {

    private final DEMAParametersV6_1 params = DEMAParametersV6_1.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV6_1(String symbol) {
        super(symbol, DEMA_V6_1.name());
    }

    @Override
    public DEMAParametersV6_1 getParams() {
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

        //ENTRY RULES
        Rule priceOverLongDEMA = debug(new OverIndicatorRule(closePrice, longIndicator));
        Rule superTrendBuy = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP, 3D), "BUY");
        Rule superTrendSlowBuy = debug(new UnderIndicatorRule(new SuperTrend(series, 20, 4D), closePrice), "BUY");
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)));
        Rule lastExit60BarsAgo = new IntervalFromLastTradeRule(60, Trade.TradeType.SELL);
        Rule oneTransaction = new PositionsCountRule(1);

        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());

        Rule lastEntry15BarsAgo = debug(new IntervalFromLastTradeRule(5, Trade.TradeType.BUY), "ENTRY 10 BARS AGO");
        Rule bollingerSell = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "BOLLINGER CROSS UP");
        Rule spikeSell = debug(bollingerSell.and(lastEntry15BarsAgo), "SPIKE SELL");

        Rule entryRule = debug(marketHours
                        .and(priceOverLongDEMA)
                        .and(superTrendBuy)
                        .and(superTrendSlowBuy)
//                        .and(oneTransaction.negation())
                        .and(lastExit60BarsAgo)
                        .and(stopTotalLossRule.negation()),
                Type.ENTRY);


        //EXIT RULES
        Rule priceOverDEMA = debug(new OverIndicatorRule(closePrice, longIndicator), "DEMA cross Down");
        Rule crossedDownDEMA = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator), "DEMA cross Down");
        Rule superTrendSell = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN, 3D), "SELL");
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandLong, closePrice));

        Rule loss2Percent = debug(new StopLossRule(closePrice, 2), "LOSS 2%");
        Rule gain3Percent = debug(new StopGainRule(closePrice, 3), "GAIN 3%");
        Rule gain1Percent = debug(new StopGainRule(closePrice, 1), "GAIN 1%");
        Rule gain05Percent = debug(new StopGainRule(closePrice, 0.5), "GAIN 0.5%");
        Rule gainAny = debug(new StopGainRule(closePrice, 0.1), "GAIN 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule downTrendWith3PercentGain = priceOverDEMA.and(gain3Percent);
        Rule downTrendWith05Gain = crossedDownDEMA.and(gain05Percent);
        Rule downTrendWithGain = priceOverLongDEMA.and(superTrendSell).and(gainAny);

        Rule exitRule = debug(spikeSell
                        .or(downTrendWith3PercentGain)
                        .or(downTrendWith05Gain)
//                        .or(downTrendWithGain)
                        .or(crossedDownDEMA.and(superTrendSell).and(chandelierOverPrice))
                        .or(loss2Percent.and(superTrendSell).and(chandelierOverPrice))
                        .or(market60MinLeft.and(gain1Percent))
                        .or(market30MinLeft.and(gainAny))
                        .or(market10MinLeft)
                        .or(stopTotalLossRule),
                Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
