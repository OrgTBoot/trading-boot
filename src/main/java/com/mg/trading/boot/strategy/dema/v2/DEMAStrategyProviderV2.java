package com.mg.trading.boot.strategy.dema.v2;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.ExtendedMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.PreMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendSellIndicator;
import com.mg.trading.boot.strategy.rules.TimeTillMarketClosesRule;
import com.mg.trading.boot.strategy.rules.TotalLossToleranceRule;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
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
public class DEMAStrategyProviderV2 implements StrategyProvider {

    private final DEMAParametersV2 params;
    private Strategy strategy;

    public DEMAStrategyProviderV2(DEMAParametersV2 params) {
        this.params = params;
    }

    public DEMAStrategyProviderV2(String symbol, BigDecimal sharesQty) {
        this.params = DEMAParametersV2.optimal(symbol, sharesQty);
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
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);


        //ENTRY RULES
        Rule preMarketHours = new BooleanIndicatorRule(new PreMarketHoursIndicator(series));
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule crossedUpDEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule chandelierUnderPrice = new UnderIndicatorRule(chandLong, closePrice);
        Rule dayMaxLossNotReached = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());

        Rule enterRule = crossedUpDEMA     // 1. trend
                .and(chandelierUnderPrice) // 2. and confirmation
                .and(dayMaxLossNotReached) // 3. and avoid entering again in a bearish stock
                .and(preMarketHours.or(marketHours)); // 4. and enter only in marked hours

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bollinger.upper());
        Rule superTrendSell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));
        Rule extendedMarketHours = new BooleanIndicatorRule(new ExtendedMarketHoursIndicator(series));
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new TimeTillMarketClosesRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator);

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(dayMaxLossNotReached.negation())          // 5. or reached day max loss percent for a given symbol
                .or(timeToMarketClose);                       // 6. or last resort rule - dump position of approaching market close

        String strategyName = "DEMAv2" + "_" + params.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
