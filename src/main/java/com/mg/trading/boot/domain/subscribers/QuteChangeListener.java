package com.mg.trading.boot.domain.subscribers;

import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.integrations.BrokerProvider;

public interface QuteChangeListener {

    void onQuoteChange(StrategyDefinition strategyDefinition, BrokerProvider broker);
}
