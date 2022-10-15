package com.mg.trading.boot.integrations;

public interface BrokerProvider {

    AccountProvider account();

    TickerDetailsProvider ticker();
}
