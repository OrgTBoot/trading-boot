package com.mg.trading.boot.strategy.ema;

import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.indicators.ExtendedMarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.MarketHoursIndicator;
import com.mg.trading.boot.strategy.indicators.PreMarketHoursIndicator;
import com.mg.trading.boot.strategy.rules.TimeTillMarketClosesRule;
import com.mg.trading.boot.strategy.rules.TotalLossToleranceRule;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;


/**
 * Apply two moving averages to a chart: one longer and one shorter.
 * When the shorter-term MA crosses above the longer-term MA, it's a buy signal, as it indicates that the trend is
 * shifting up. This is known as a golden cross. Meanwhile, when the shorter-term EMA crosses below the
 * longer-term EMA, it's a sell signal, as it indicates that the trend is shifting down
 * <p>
 * For more details see: <a href="https://www.youtube.com/watch?v=6mckJdktXkc">Golden Cross</a>
 */
@Log4j2
public class EMAStrategyProvider implements StrategyProvider {

    private final EMAParameters parameters;
    private Strategy strategy;

    public EMAStrategyProvider(EMAParameters parameters) {
        this.parameters = parameters;
    }

    public EMAStrategyProvider(String symbol, BigDecimal sharesQty) {
        this.parameters = EMAParameters.optimal(symbol, sharesQty);
    }

    @Override
    public EMAParameters getParameters() {
        return this.parameters;
    }

    @Override
    public Strategy getStrategy() {
        return this.strategy;
    }

    @Override
    public StrategyProvider buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortIndicator = new EMAIndicator(closePrice, parameters.getShortBarCount());
        EMAIndicator longIndicator = new EMAIndicator(closePrice, parameters.getLongBarCount());

        //entry
        CrossedUpIndicatorRule crossedUpEMA = new CrossedUpIndicatorRule(shortIndicator, longIndicator);
        Rule preMarketHours = new BooleanIndicatorRule(new PreMarketHoursIndicator(series));
        Rule marketHours = new BooleanIndicatorRule(new MarketHoursIndicator(series));
        Rule dayMaxLossNotReached = new TotalLossToleranceRule(series, parameters.getTotalLossTolerancePercent());

        Rule enterRule = crossedUpEMA
                .and(dayMaxLossNotReached)
                .and(preMarketHours.or(marketHours));

        //exit
        Rule extendedMarketHours = new BooleanIndicatorRule(new ExtendedMarketHoursIndicator(series));
        Rule hasMinimalProfit = new StopGainRule(closePrice, 0.1);
        Rule timeToMarketClose = new TimeTillMarketClosesRule(series, parameters.getMinutesToMarketClose(), TimeUnit.MINUTES);

        Rule exitRule = dayMaxLossNotReached.negation()
                .or(extendedMarketHours.and(hasMinimalProfit))
                .or(timeToMarketClose);

        String strategyName = "EMA" + "_" + parameters.getSymbol();
        this.strategy = new BaseStrategy(strategyName, enterRule, exitRule);
        return this;
    }
}
