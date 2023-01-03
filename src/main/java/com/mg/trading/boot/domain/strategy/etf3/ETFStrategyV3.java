package com.mg.trading.boot.domain.strategy.etf3;

import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import com.mg.trading.boot.domain.rules.*;
import com.mg.trading.boot.domain.rules.TracingRule.Type;
import com.mg.trading.boot.domain.strategy.AbstractStrategyDefinition;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;


/**
 * For more details see: <a href="https://www.youtube.com/watch?v=Jd1JVF7Oy_A">Double EMA Cross</a>
 * For more details see: <a href="https://www.youtube.com/watch?v=g-PLctW8aU0">Double EMA Cross + Fibonacci</a>
 */
@Log4j2
public class ETFStrategyV3 extends AbstractStrategyDefinition {

    private final ETFParametersV3 params = ETFParametersV3.optimal();
    private Strategy strategy;

    public ETFStrategyV3(String symbol) {
        super(symbol, "ETFV3");
    }

    @Override
    public ETFParametersV3 getParams() {
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
        EMAIndicator shortEmaInd = new EMAIndicator(closePrice, 9);
        EMAIndicator longEmaInd = new EMAIndicator(closePrice, 26);
        MACDIndicator macdInd = new MACDIndicator(closePrice, 9, 26);
        EMAIndicator emaMacdInd = new EMAIndicator(macdInd, 18);
        StochasticOscillatorKIndicator stochasticInd = new StochasticOscillatorKIndicator(series, 14);
        BollingerBandFacade bollinger = new BollingerBandFacade(series, params.getBollingerBarCount(), params.getBollingerMultiplier());

        //COMMON RULES
        Rule market60MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES), "MKT 60min left");
        Rule marketHours = debug(new MarketHoursRule(series).and(market60MinLeft.negation()), "MKT HOURS");
        Rule stopTotalLossRule = debug(new StopTotalLossRule(series, BigDecimal.valueOf(3)));
        Rule bollingerCrossUp = debug(new OverIndicatorRule(closePrice, bollinger.upper()), "Bollinger cross Up");

        //ENTRY RULES
        Rule demaBuy = debug(new OverIndicatorRule(shortEmaInd, longEmaInd), "DEMA BUY");
        Rule stochasticBuy = debug(new CrossedDownIndicatorRule(stochasticInd, 20), "STOCHASTIC BUY");
        Rule macdBuy = debug(new OverIndicatorRule(macdInd, emaMacdInd), "MACD BUY");

        Rule entryRule = marketHours
                .and(demaBuy)       // Trend
                .and(stochasticBuy) // Signal 1
                .and(macdBuy);      // Signal 2

        //EXIT RULES
        Rule has1PercentGain = debug(new StopGainRule(closePrice, 1), "Has > 1%");
        Rule hasAnyGain = debug(new StopGainRule(closePrice, 0.1), "Gain > 0.1%");
        Rule market30MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 30, TimeUnit.MINUTES), "MKT 30min left");
        Rule market10MinLeft = debug(new MarketTimeLeftRule(series, MARKET_HOURS, 10, TimeUnit.MINUTES), "MKT 10min left");
        Rule trailingLoss1 = debug(new TrailingStopLossRule(closePrice, series.numOf(1)), "TRAILING LOSS SELL");
        Rule trailingLoss05 = debug(new TrailingStopLossRule(closePrice, series.numOf(0.25)), "TRAILING LOSS SELL");
        Rule demaSell = new UnderIndicatorRule(shortEmaInd, longEmaInd);
        Rule stochasticSell = new CrossedUpIndicatorRule(stochasticInd, 80);
        Rule macdSell = new UnderIndicatorRule(macdInd, emaMacdInd);

        Rule exitRule = demaSell     // Trend
                .and(stochasticSell) // Signal 1
                .and(macdSell)       // Signal 2
                .or(hasAnyGain.and(bollingerCrossUp.or(trailingLoss1).or(market30MinLeft)))
                .or(market10MinLeft)
                .or(stopTotalLossRule);

        return new BaseStrategy(getStrategyName(), entryRule, exitRule);
    }


}
