package com.mg.trading.boot.domain.strategy.dema;

import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.strategy.XAbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class XDEMAStrategyDefinition extends XAbstractStrategyDefinition {

    private final XDEMAParameters params = XDEMAParameters.optimal();
    private Strategy strategy;

    public XDEMAStrategyDefinition(String symbol) {
        super(symbol, "DEMA");
    }

    @Override
    public XDEMAParameters getParams() {
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
//        //INDICATORS
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
//        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
//        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
//
//        //ENTRY RULES
//        Rule crossedUp = new XCrossedUpIndicatorRule(shortIndicator, longIndicator);
//        Rule marketHours = new XMarketHoursRule(series).or(new XPreMarketHoursRule(series));
//        Rule dayMaxLossNotReached = new XUnderTotalLossThresholdRule(series, params.getTotalLossThresholdPercent());
//
//        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
//        Rule entryRule = crossedUp.and(marketHours).and(dayMaxLossNotReached);
//
//        //EXIT RULES
//        Rule bollingerCrossUp = new XOverIndicatorRule(closePrice, bandFacade.upper());
//        Rule crossedDownDEMA = new XCrossedDownIndicatorRule(shortIndicator, longIndicator);
//        Rule superTrendSell = new XSuperTrendSellRule(series, params.getShortBarCount());
//        Rule extendedMarketHours = new XExtendedMarketHoursRule(series);
//        Rule hasMinimalProfit = new XStopGainRule(closePrice, 0.1);
//        Rule timeToMarketClose = new XTimeToMarketExtendedHoursRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);
//
//        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
//                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
//                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
//                .or(dayMaxLossNotReached.negation())          // 5. or reached day max loss percent for a given symbol
//                .or(timeToMarketClose);                       // 6. or last resort rule - dump position of approaching market close

        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());

        //ENTRY RULES
        Rule crossedUp = new XCrossedUpIndicatorRule(shortIndicator, longIndicator);
//        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule marketHours = new XMarketHoursRule(series);
        Rule sessionLossTolerance = new XUnderTotalLossThresholdRule(series, params.getTotalLossThresholdPercent());

        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
        Rule enterRule = crossedUp.and(marketHours).and(sessionLossTolerance);

        //EXIT RULES
        Rule bollingerCrossUp = new XOverIndicatorRule(closePrice, bandFacade.upper());
        Rule crossedDownDEMA = new XCrossedDownIndicatorRule(shortIndicator, longIndicator);
//        Rule superTrendSell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));
        Rule superTrendSell = new XSuperTrendSellRule(series, params.getShortBarCount());
        Rule dayMaxLossNotReached = new XUnderTotalLossThresholdRule(series, params.getTotalLossThresholdPercent());
        Rule extendedMarketHours = new XMarketExtendedHoursRule(series);
        Rule hasMinimalProfit = new XStopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new XMarketTimeToExtendedHoursCloseRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(dayMaxLossNotReached.negation())          // 5. or reached day max loss percent for a given symbol
                .or(timeToMarketClose);                       // 6. or last resort rule - dump position of approaching market close

        return new BaseStrategy(getStrategyName(), enterRule, exitRule);
    }

}
