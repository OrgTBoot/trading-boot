package com.mg.trading.boot.strategy.ema;

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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=6mckJdktXkc">Golden Cross</a>
 */
@Log4j2
public class EMAStrategyProvider implements StrategyProvider {

    private final EMAParameters params;
    private Strategy strategy;

    public EMAStrategyProvider(EMAParameters params) {
        this.params = params;
    }

    public EMAStrategyProvider(String symbol, BigDecimal sharesQty) {
        this.params = EMAParameters.optimal(symbol, sharesQty);
    }

    @Override
    public EMAParameters getParams() {
        return this.params;
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortIndicator = new EMAIndicator(closePrice, params.getShortBarCount());
        EMAIndicator longIndicator = new EMAIndicator(closePrice, params.getLongBarCount());

        //entry
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getLongBarCount(), params.getBollingerMultiplier());
        CrossedUpIndicatorRule crossedUpEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule preMarketHours = new BooleanIndicatorRule(new PreMarketHoursIndicator(series));
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule dayMaxLossNotReached = new TotalLossToleranceRule(series, params.getTotalLossTolerancePercent());

        Rule enterRule = crossedUpEMA
                .and(dayMaxLossNotReached)
                .and(preMarketHours.or(marketHours));

        //exit
        Rule bollingerCrossUp = new OverIndicatorRule(closePrice, bollinger.upper());
        Rule crossedDownDEMA = new CrossedDownIndicatorRule(shortIndicator, longIndicator);
        Rule superTrendSell = new BooleanIndicatorRule(new SuperTrendSellIndicator(series, params.getShortBarCount()));
        Rule extendedMarketHours = new BooleanIndicatorRule(new ExtendedMarketHoursIndicator(series));
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new TimeTillMarketClosesRule(series, params.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule exitRule = bollingerCrossUp                      // 1. trend reversal signal, reached upper line, market will start selling
                .or(crossedDownDEMA.and(superTrendSell))      // 2. or down-trend and sell confirmation
                .or(extendedMarketHours.and(hasMinimalProfit))// 3. or try to exit in after marked with some profit
                .or(dayMaxLossNotReached.negation())          // 5. or reached day max loss percent for a given symbol
                .or(timeToMarketClose);                       // 6. or last resort rule - dump position of approaching market close

        String strategyName = "EMA" + "_" + params.getSymbol();
        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }
}
