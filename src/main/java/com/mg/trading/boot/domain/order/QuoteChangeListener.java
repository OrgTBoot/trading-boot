package com.mg.trading.boot.domain.order;

import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.integrations.BrokerProvider;

public interface QuoteChangeListener {

    void onQuoteChange(StrategyDefinition strategyDefinition, BrokerProvider broker);
}
