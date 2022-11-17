package com.mg.trading.boot.domain.strategy.dema9;

import com.mg.trading.boot.domain.rules.MarketHoursRule;
import com.mg.trading.boot.domain.rules.MarketTimeLeftRule;
import com.mg.trading.boot.domain.rules.StopTotalLossRule;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ChandelierExitLongIndicator;
import org.ta4j.core.indicators.ZLEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

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
        Rule marketHours = trace(new MarketHoursRule(series));
        Rule market60MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule maxTotalLoss = trace(new StopTotalLossRule(series, params.getTotalLossThresholdPercent()));
        Rule chandelierUnderPrice = trace(new UnderIndicatorRule(chandelier, closePrice));
        Rule zlEmaUnderPrice = trace(new UnderIndicatorRule(zlemaIndicator, closePrice));

        Rule entryRule = trace(
                marketHours
                        .and(market60MinLeft.negation())
                        .and(maxTotalLoss.negation())
                        .and(chandelierUnderPrice)
                        .and(zlEmaUnderPrice)
                , Type.ENTRY);


        //EXIT RULES
        Rule chandelierOverPrice = trace(new OverIndicatorRule(chandelier, closePrice));
        Rule zlEmaOverPrice = trace(new OverIndicatorRule(zlemaIndicator, closePrice));

        Rule bollingerCrossUp = trace(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");
        Rule has1PercentGain = trace(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyGain = trace(new StopGainRule(closePrice, 0.1), "Has > 0.1%");
        Rule market30MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = trace(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");

        Rule exitRule = trace(
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
