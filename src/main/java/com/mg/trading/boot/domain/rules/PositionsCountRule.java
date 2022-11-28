package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;


@Log4j2
public class PositionsCountRule extends AbstractRule implements Rule {
    private final int value;

    public PositionsCountRule(int value) {
        this.value = value;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;

        if (tradingRecord == null) {
            traceIsSatisfied(index, false);
            return false;
        }

        if (tradingRecord.getPositions().size() >= value) {
            satisfied = true;
        }
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }

}
