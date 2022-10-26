package com.mg.trading.boot.domain.rules;

import lombok.extern.log4j.Log4j2;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

@Log4j2
public class TracingRule extends AbstractRule {
    private final Rule rule;
    private final String alias;
    private final Type type;

    public enum Type {
        ENTRY, EXIT, CHAIN
    }

    public TracingRule(Rule rule, Type type) {
        this.rule = rule;
        this.alias = "";
        this.type = type;
    }

    public TracingRule(Rule rule, Type type, String alias) {
        this.rule = rule;
        this.alias = alias;
        this.type = type;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = rule.isSatisfied(index, tradingRecord);

        log.debug("{} {} {} {} satisfied {}", index, type, rule.getClass().getSimpleName(), alias, satisfied);

        if (!Type.CHAIN.equals(type)) {
            log.debug("-----------------------------------------------------");
        }
        return satisfied;
    }
}
