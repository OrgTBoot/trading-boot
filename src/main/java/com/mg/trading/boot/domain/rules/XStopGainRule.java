package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.rules.indicators.markethours.XMarketHoursIndicator;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.StopGainRule;

@Log4j2
public class XStopGainRule extends StopGainRule implements IRule {
    public XStopGainRule(ClosePriceIndicator closePrice, Number gainPercentage) {
        super(closePrice, gainPercentage);
    }

    public XStopGainRule(ClosePriceIndicator closePrice, Num gainPercentage) {
        super(closePrice, gainPercentage);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = super.isSatisfied(index, tradingRecord);
        logResult(log, satisfied, index);

        return satisfied;
    }
}
