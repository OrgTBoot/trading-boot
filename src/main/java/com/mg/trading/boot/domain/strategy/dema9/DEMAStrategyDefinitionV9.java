package com.mg.trading.boot.domain.strategy.dema9;

import com.mg.trading.boot.domain.rules.MarketHoursRule;
import com.mg.trading.boot.domain.rules.MarketTimeLeftRule;
import com.mg.trading.boot.domain.rules.StopTotalLossRule;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.ZLEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;

/**
 * <a href="https://www.youtube.com/watch?v=mfjrkm4YRwE">Zero Lag Indicator</a>
 */
@Log4j2
public class DEMAStrategyDefinitionV9 extends AbstractStrategyDefinition {

    private final DEMAParametersV9 params = DEMAParametersV9.optimal();
    private Strategy strategy;

    public DEMAStrategyDefinitionV9(String symbol) {
        super(symbol, "DEMAV9");
    }

    @Override
    public DEMAParametersV9 getParams() {
        return params;
    }

    @Override
    public void setSeries(BarSeries series) {
        this.series = series;
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
        ZLEMAIndicator zlemaIndicator = new ZLEMAIndicator(closePrice, params.getLongBarCount());
        ChandelierExitLongIndicator chandelier = new ChandelierExitLongIndicator(series, params.getCndBarCount(), params.getCndMultiplier());

        //ENTRY RULES
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
        Rule maxTotalLoss = debug(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule chandelierUnderPrice = debug(new UnderIndicatorRule(chandelier, closePrice));
        Rule zlEmaUnderPrice = debug(new UnderIndicatorRule(zlemaIndicator, closePrice));

        Rule hasTotal4PercentLoss = debug(new StopTotalLossRule(series, BigDecimal.valueOf(4)));

        Rule entryRule = debug(
                marketHours
//                        .and(maxTotalLoss.negation())
                        .and(hasTotal4PercentLoss.negation())//TODO: this parameter was set based on results from data sets - we should not trust it yet. More testing TBD.
                        .and(chandelierUnderPrice)
                        .and(zlEmaUnderPrice)
                , Type.ENTRY);


        //EXIT RULES
        Rule chandelierOverPrice = debug(new OverIndicatorRule(chandelier, closePrice));
        Rule zlEmaOverPrice = debug(new OverIndicatorRule(zlemaIndicator, closePrice));

        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule has1PercentGain = debug(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyGain = debug(new StopGainRule(closePrice, 0.1), "Has > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = debug(
                bollingerCrossUp
                        .or(chandelierOverPrice.and(zlEmaOverPrice))
                        .or(market60MinLeft.and(has1PercentGain))
                        .or(market30MinLeft.and(hasAnyGain))
                        .or(market10MinLeft)
                        .or(maxTotalLoss)
                , Type.EXIT);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }

}
