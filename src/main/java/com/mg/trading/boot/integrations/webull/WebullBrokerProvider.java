package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.TickerDetailsProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


@Log4j2
@Component
public class WebullBrokerProvider implements BrokerProvider {
    private final AccountProvider accountProvider;
    private final TickerDetailsProvider tickerDetailsProvider;

    public WebullBrokerProvider(AccountProvider accountProvider,
                                TickerDetailsProvider tickerDetailsProvider) {
        this.accountProvider = accountProvider;
        this.tickerDetailsProvider = tickerDetailsProvider;
    }

    @Override
    public AccountProvider account() {
        return this.accountProvider;
    }

    @Override
    public TickerDetailsProvider ticker() {
        return this.tickerDetailsProvider;
    }

}
