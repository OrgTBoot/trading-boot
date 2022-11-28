package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Rule;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;


@Log4j2
public class IntervalFromLastTradeRule extends AbstractRule implements Rule {
    int barsInterval;
    Trade.TradeType type;

    public IntervalFromLastTradeRule(int barsInterval, Trade.TradeType type) {
        this.barsInterval = barsInterval;
        this.type = type;
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;

        if (tradingRecord == null) {
            satisfied = true;

        } else {
            Trade lastTrade = type.equals(Trade.TradeType.BUY) ? tradingRecord.getLastEntry() : tradingRecord.getLastExit();
            if (lastTrade == null) {
                satisfied = true;
            } else {
                if (index - barsInterval >= lastTrade.getIndex()) {
                    satisfied = true;
                }
            }
        }

        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

}
