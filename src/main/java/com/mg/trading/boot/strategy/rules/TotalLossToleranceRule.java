package com.mg.trading.boot.strategy.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;

/**
 * Rule is considered satisfied if total loss is under predefined threshold.
 */
@Log4j2
public class TotalLossToleranceRule implements Rule {
    private final BarSeries series;
    private final Number lossTolerance;

    public TotalLossToleranceRule(BarSeries series, Number lossTolerance) {
        this.series = series;
        this.lossTolerance = lossTolerance.doubleValue() * -1;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (tradingRecord == null) {
            return true;
        }

        Num currentLoss = new ProfitLossPercentageCriterion().calculate(series, tradingRecord);
        boolean underThreshold = lossTolerance.doubleValue() <= currentLoss.doubleValue();

        if (!underThreshold) {
            log.warn("You've reached total loss tolerance threshold. Current {}, allowed is {}. Idx={}",
                    currentLoss.doubleValue(),
                    lossTolerance.doubleValue(),
                    index);
        }
        return underThreshold;
    }
}
