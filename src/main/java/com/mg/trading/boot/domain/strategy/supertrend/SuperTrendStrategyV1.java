package com.mg.trading.boot.domain.strategy.supertrend;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.ZLEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;

@Log4j2
public class SuperTrendStrategyV1 extends AbstractStrategyDefinition {

    private final SuperTrendParametersV1 params = SuperTrendParametersV1.optimal();
    private Strategy strategy;

    public SuperTrendStrategyV1(String symbol) {
        super(symbol, "ST1");
    }

    @Override
    public SuperTrendParametersV1 getParams() {
        return params;
    }

    @Override
    public Strategy getStrategy() {
        if (strategy == null) {
            this.strategy = initStrategy();
        }
        return strategy;
    }

    public void setSeries(BarSeries series) {
        super.series = series;
    }


    private Strategy initStrategy() {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator demaShortInd = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator demaLongInd = new DoubleEMAIndicator(closePrice, params.getLongBarCount());

        //ENTRY RULES
        Rule marketHours = debug(new MarketHoursRule(series));
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");

        SuperTrend st10_3Ind = new SuperTrend(series, 10, 3D);
        SuperTrend st10_4Ind = new SuperTrend(series, 10, 4D);
        SuperTrend st20_4Ind = new SuperTrend(series, 20, 4D);

        Rule st10_3Buy = debug(new UnderIndicatorRule(st10_3Ind, closePrice), "st10_3Buy");
        Rule st20_4Buy = debug(new UnderIndicatorRule(st20_4Ind, closePrice), "st20_4Buy");

        Rule st10_3Sell = debug(new OverIndicatorRule(st10_3Ind, closePrice), "st10_3Sell");
        Rule st20_4Sell = debug(new OverIndicatorRule(st20_4Ind, closePrice), "st20_4Sell");

        Rule demaBuy = debug(new UnderIndicatorRule(demaLongInd, demaShortInd).and(new UnderIndicatorRule(demaLongInd, closePrice)), "DEMA BUY");
        Rule demaSell = debug(new OverIndicatorRule(demaLongInd, demaShortInd), "DEMA SELL");

        Rule entryRule = debug(marketHours
                        .and(market60MinLeft.negation())
                        .and(demaBuy)
                        .and(st10_3Buy)
                        .and(st20_4Buy)
                , Type.ENTRY);


        //EXIT RULES
        Rule gain05Percent = debug(new StopGainRule(closePrice, 0.5));
        Rule gain3Percent = debug(new StopGainRule(closePrice, 3));

        Rule downTrend3PercentGain = debug(gain3Percent).and(demaSell);
        Rule downTrend1PercentGain = debug(gain05Percent).and(demaSell).and(st10_3Sell);
        Rule downTrend = st20_4Sell.and(st10_3Sell).and(demaSell);

        Rule exitRule = debug(
                downTrend3PercentGain
                        .or(downTrend1PercentGain)
//                        .or(downTrend)
//                        .or(market60MinLeft.and(position1PercentGain))
//                        .or(market30MinLeft.and(positionAnyGain))
//                        .or(market10MinLeft)
                , Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }
}
