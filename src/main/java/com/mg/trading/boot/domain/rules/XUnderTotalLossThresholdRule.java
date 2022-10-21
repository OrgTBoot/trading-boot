package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;

/**
 * Rule is considered satisfied if total loss is under predefined threshold.
 */
@Log4j2
public class XUnderTotalLossThresholdRule implements IRule {
    private final BarSeries series;
    private final Number threshold;

    public XUnderTotalLossThresholdRule(BarSeries series, Number threshold) {
        this.series = series;
        this.threshold = threshold.doubleValue() * -1;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (tradingRecord == null) {
            logResult(log, Boolean.FALSE, index);
            return false;

        }

        Num currentLoss = new ProfitLossPercentageCriterion().calculate(series, tradingRecord);
        boolean underThreshold = threshold.doubleValue() <= currentLoss.doubleValue();

        if (!underThreshold) {
            log.warn("You've reached total loss tolerance threshold. Current {}, allowed is {}. Idx={}",
                    currentLoss.doubleValue(),
                    threshold.doubleValue(),
                    index);
        }
        logResult(log, underThreshold, index);

        return underThreshold;
    }
}
