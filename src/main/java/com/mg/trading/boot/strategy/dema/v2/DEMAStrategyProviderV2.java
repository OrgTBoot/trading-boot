package com.mg.trading.boot.strategy.dema.v2;

import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.AfterMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.SuperTrendSellIndicator;
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
    public StrategyParameters getParameters() {
        return this.params;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        //INDICATORS
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, params.getShortBarCount());
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, params.getLongBarCount());
        BollingerBandFacade bandFacade = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        ChandelierExitLongIndicator chandLong = new ChandelierExitLongIndicator(series, params.getChandelierBarCount(), 3);


        //ENTRY RULES
        Rule crossedUpDEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule chandelierUnderPrice = new UnderIndicatorRule(chandLong, closePrice);
        Rule inMarketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule dayMaxLossNotReached = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());

        Rule enterRule = crossedUpDEMA.and(chandelierUnderPrice).and(inMarketHours).and(dayMaxLossNotReached);

        //EXIT RULES
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bandFacade.upper());
        Rule afterMarketHours = new BooleanIndicatorRule(new AfterMarketHoursIndicator(series));
        Rule hasProfit = new StopGainRule(closePrice, 0.3); //todo: this is not working when strategy is restarted
        Rule superTrendSell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));

        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator);

        Rule exitRule = bollingerCrossUp
                .or(crossedDownDEMA.and(superTrendSell))
                .or(afterMarketHours.and(hasProfit));

        String strategyName = "DEMAv2" + "_" + params.getSymbol();

        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }

}
