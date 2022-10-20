package com.mg.trading.boot.strategy.dema.v1;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.ExtendedMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendGreenIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendSellIndicator;
import com.mg.trading.boot.strategy.rules.TimeTillMarketClosesRule;
import com.mg.trading.boot.strategy.rules.TotalLossToleranceRule;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class DEMAStrategyProvider implements StrategyProvider {

    private final DEMAParameters params;
    private Strategy strategy;

    public DEMAStrategyProvider(DEMAParameters params) {
        this.params = params;
    }

    public DEMAStrategyProvider(String symbol, BigDecimal sharesQty) {
        this.params = DEMAParameters.optimal(symbol, sharesQty);
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyParameters getParams() {
        return this.params;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());

        //ENTRY RULES
        Rule crossedUp = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule sessionLossTolerance = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());

        //todo: crossedUp needs a confirmation - we need second indicator that can act as a confirmation
        Rule enterRule = crossedUp.and(marketHours).and(sessionLossTolerance);

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bandFacade.upper());
        Rule dayMaxLossNotReached = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());
        Rule extendedMarketHours = new BooleanIndicatorRule(new ExtendedMarketHoursIndicator(series));
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new TimeTillMarketClosesRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule exitRule = bollingerCrossUp
                .or(extendedMarketHours.and(hasMinimalProfit))
                .or(dayMaxLossNotReached.negation())
                .or(timeToMarketClose);

        String strategyName = "DEMA" + "_" + params.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
