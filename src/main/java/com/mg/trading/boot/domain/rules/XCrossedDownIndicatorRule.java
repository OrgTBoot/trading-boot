package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

@Log4j2
public class XCrossedDownIndicatorRule extends CrossedDownIndicatorRule implements Rule {


    public XCrossedDownIndicatorRule(Indicator<Num> indicator, Number threshold) {
        super(indicator, threshold);
    }

    public XCrossedDownIndicatorRule(Indicator<Num> indicator, Num threshold) {
        super(indicator, threshold);
    }

    public XCrossedDownIndicatorRule(Indicator<Num> first, Indicator<Num> second) {
        super(first, second);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        return super.isSatisfied(index, tradingRecord);
    }
}
