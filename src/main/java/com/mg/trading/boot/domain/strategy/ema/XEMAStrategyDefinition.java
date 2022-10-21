package com.mg.trading.boot.domain.strategy.ema;

import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.strategy.XAbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class XEMAStrategyDefinition extends XAbstractStrategyDefinition {

    private final XEMAParameters params = XEMAParameters.optimal();
    private Strategy strategy;

    public XEMAStrategyDefinition(String symbol) {
        super(symbol, "EMA");
    }

    @Override
    public XEMAParameters getParams() {
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
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortIndicator = new EMAIndicator(closePrice, params.getShortBarCount());
        EMAIndicator longIndicator = new EMAIndicator(closePrice, params.getLongBarCount());

        //entry
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        CrossedUpIndicatorRule crossedUpEMA = new XCrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule marketHours = new XMarketHoursRule(series).or(new XMarketPreHoursRule(series));
        Rule dayMaxLossNotReached = new XUnderTotalLossThresholdRule(series, params.getTotalLossThresholdPercent());

        Rule enterRule = crossedUpEMA
                .and(dayMaxLossNotReached)
                .and(marketHours);

        //exit
        Rule bollingerCrossUp = new XOverIndicatorRule(closePrice, bollinger.upper());
        Rule crossedDownDEMA = new XCrossedDownIndicatorRule(shortIndicator, longIndicator);
        Rule superTrendSell = new XSuperTrendSellRule(series, params.getShortBarCount());
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
