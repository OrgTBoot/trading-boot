package com.mg.trading.boot.domain.rules;

import org.apache.logging.log4j.Logger;
import org.ta4j.core.Rule;


public interface IRule extends Rule {

    default void logResult(Logger logger, boolean satisfied, int barIdx) {
        String msg = String.format("Satisfied %s at idx %s", satisfied, barIdx);
        logger.trace(msg);
    }
}
