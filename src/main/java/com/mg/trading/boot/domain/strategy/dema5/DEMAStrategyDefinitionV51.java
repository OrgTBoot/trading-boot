package com.mg.trading.boot.domain.strategy.dema5;

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
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;

@Log4j2
public class DEMAStrategyDefinitionV51 extends AbstractStrategyDefinition {

    private final DEMAParametersV5 params = DEMAParametersV5.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV51(String symbol) {
        super(symbol, "DEMAV5");
    }

    @Override
    public DEMAParametersV5 getParams() {
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
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());

        //ENTRY RULES
        Rule priceOverLongDEMA = debug(new OverIndicatorRule(closePrice, longIndicator));
        Rule superTrendUpSignalUp = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.UP, Signal.UP), "BUY");
        Rule marketHours = debug(new MarketHoursRule(series));
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");

        Rule consecutiveLosses3 = debug(new ConsecutiveLossPositionsRule(series, 3), "3 CONS LOSSES");
        Rule positionLoss3Percent = debug(new StopLossRule(closePrice, 3), "LOSS 6%");
        Rule totalLossPercent4 = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)), "LOSS 4%");
        Rule totalLossPercent6 = debug(new StopTotalLossRule(series, BigDecimal.valueOf(6)), "LOSS 6%");

        Rule entryRule = debug(marketHours
                        .and(market60MinLeft.negation())
                        .and(priceOverLongDEMA)
                        .and(superTrendUpSignalUp)
                        .and(consecutiveLosses3.negation())
                        .and(totalLossPercent4.negation()),
                Type.ENTRY);


        //EXIT RULES
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule crossedDownDEMA = debug(new CrossedDownIndicatorRule(shortIndicator, longIndicator), "DEMA cross Down");
        Rule superTrendSell = debug(new SuperTrendRule(series, params.getShortBarCount(), Trend.DOWN, Signal.DOWN), "SELL");

        Rule has5PercentLoss = debug(new StopLossRule(closePrice, 2), "Has -2%");
        Rule has1PercentProfit = debug(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyProfit = debug(new StopGainRule(closePrice, 0.1), "Has > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule priceCrossedDownDEMA = new CrossedDownIndicatorRule(closePrice, longIndicator);
        Rule exitRule = debug(
                bollingerCrossUp                                      // 1. trend reversal signal, reached upper line, market will start selling
                        .or(crossedDownDEMA.and(superTrendSell).and((priceCrossedDownDEMA))) // 2. confirmation
                        .or(has5PercentLoss.and(superTrendSell).and((priceCrossedDownDEMA))) // 3. position stop loss
                        .or(market60MinLeft.and(has1PercentProfit))   // 4. or 60m to market close, take profits >= 1%
                        .or(market30MinLeft.and(hasAnyProfit))        // 5. or 30m to market close, take any profits > 0%
                        .or(market10MinLeft)
//                        .or(positionLoss3Percent)
                        .or(totalLossPercent6), Type.EXIT);           // 7. or reached day max loss percent for a given symbol

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
