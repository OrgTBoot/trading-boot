package com.mg.trading.boot.domain.subscribers;

import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.integrations.BrokerProvider;

public interface QuteChangeListener {

    void onQuoteChange(IStrategyDefinition strategyDefinition, BrokerProvider broker);
}
