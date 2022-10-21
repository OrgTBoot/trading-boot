package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.StopLossRule;

@Log4j2
public class XStopLossRule extends StopLossRule implements Rule {

    public XStopLossRule(ClosePriceIndicator closePrice, Number lossPercentage) {
        super(closePrice, lossPercentage);
    }

    public XStopLossRule(ClosePriceIndicator closePrice, Num lossPercentage) {
        super(closePrice, lossPercentage);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        return super.isSatisfied(index, tradingRecord);
    }
}
