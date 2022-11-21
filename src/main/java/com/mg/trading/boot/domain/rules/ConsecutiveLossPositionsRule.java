package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.utils.NumberUtils;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

import java.math.BigDecimal;


@Log4j2
public class ConsecutiveLossPositionsRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final int lossPositionsThreshold;

    public ConsecutiveLossPositionsRule(BarSeries series, int lossPositionsThreshold) {
        this.series = series;
        this.lossPositionsThreshold = lossPositionsThreshold;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;

        if (tradingRecord == null) {
            traceIsSatisfied(index, false);
            return false;
        }

        int counter = 0;
        for (Position position : tradingRecord.getPositions()) {
            if (position.hasLoss()) {
                counter++;

                if (counter == this.lossPositionsThreshold) {
                    satisfied = true;
                    break;
                }
            } else {
                counter = 0;
            }
        }

        if (satisfied) {
            log.warn("You've reached total consecutive lost positions: {}", counter);
        }

        traceIsSatisfied(index, satisfied);

        return satisfied;
    }

}
