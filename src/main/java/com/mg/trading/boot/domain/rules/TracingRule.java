package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

import java.util.Date;

@Log4j2
public class TracingRule extends AbstractRule {
    private final Rule rule;
    private final String alias;
    private final Type type;
    private final BarSeries series;

    public enum Type {
        ENTRY, EXIT, CHAIN
    }

    public TracingRule(Rule rule, Type type, BarSeries series) {
        this.rule = rule;
        this.alias = "";
        this.type = type;
        this.series = series;
    }

    public TracingRule(Rule rule, Type type, String alias, BarSeries series) {
        this.rule = rule;
        this.alias = alias;
        this.type = type;
        this.series = series;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = rule.isSatisfied(index, tradingRecord);

        log.debug("{} {} {} {} satisfied {}", index, type, rule.getClass().getSimpleName(), alias, satisfied);

        if (!Type.CHAIN.equals(type)) {
            Bar bar = series.getBar(index);
            long timeStampMls = bar.getEndTime().toInstant().toEpochMilli();
            Date date = Date.from(bar.getEndTime().toInstant());

            log.debug("{} Bar time={} | {}, closePrice={}", index, date, timeStampMls, bar.getClosePrice());
            log.debug("-----------------------------------------------------");
        }
        return satisfied;
    }
}
