package com.mg.trading.boot.domain.strategy.supertrend;

import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.rules.MarketHoursRule;
import com.mg.trading.boot.domain.rules.MarketTimeLeftRule;
import com.mg.trading.boot.domain.rules.StopTotalLossRule;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ZLEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

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

    private Strategy initStrategy() {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());
        SuperTrend superTrendShort = new SuperTrend(series, 10, 3D);
        SuperTrend superTrendMed = new SuperTrend(series, 15, 4D);
        SuperTrend superTrendLong = new SuperTrend(series, 20, 5D);

        //ENTRY RULES
        Rule marketHours = debug(new MarketHoursRule(series));
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule superTrendShortBuy = debug(new UnderIndicatorRule(superTrendShort, closePrice), "ST BUY S");
        Rule superTrendMedBuy = debug(new UnderIndicatorRule(superTrendMed, closePrice), "ST BUY M");
        Rule superTrendLongBuy = debug(new UnderIndicatorRule(superTrendLong, closePrice), "ST BUY L");

        Rule totalLoss4Percent = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)), "LOSS 4%");
        Rule totalLoss6Percent = debug(new StopTotalLossRule(series, BigDecimal.valueOf(6)), "LOSS 6%");

        Rule entryRule = debug(marketHours
                        .and(market60MinLeft.negation())
                        .and(superTrendLongBuy)
                        .and(superTrendMedBuy)
                        .and(superTrendShortBuy)
                        .and(totalLoss4Percent.negation())
                , Type.ENTRY);


        //EXIT RULES
        Rule superTrendLongSell = debug(new OverIndicatorRule(superTrendLong, closePrice), "ST SELL S");
        Rule superTrendMedSell = debug(new OverIndicatorRule(superTrendMed, closePrice), "ST SELL M");
        Rule superTrendShortSell = debug(new OverIndicatorRule(superTrendShort, closePrice), "ST SELL L");

        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule position1PercentGain = debug(new StopGainRule(closePrice, 1), "GAIN 1%");
        Rule positionAnyGain = debug(new StopGainRule(closePrice, 0.1), "GAIN 0.1%");
        Rule positionLoss3Percent = debug(new StopLossRule(closePrice, 3), "LOSS 6%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = debug(
                bollingerCrossUp
                        .or(superTrendLongSell.and(superTrendShortSell).and(superTrendMedSell))
                        .or(superTrendLongSell.and(positionAnyGain))
                        .or(market60MinLeft.and(position1PercentGain))
                        .or(market30MinLeft.and(positionAnyGain))
                        .or(market10MinLeft)
                        .or(positionLoss3Percent)
                        .or(totalLoss6Percent), Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
