package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.models.Account;
import com.mg.trading.boot.models.Order;
import com.mg.trading.boot.models.OrderRequest;
import com.mg.trading.boot.models.Position;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.TradingRecord;

import java.util.List;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;

@Log4j2
@Component
@ConditionalOnProperty(value = "features.paper-trading", havingValue = "false")
public class WTradingAccountProvider extends AbstractRestProvider implements AccountProvider {
    private final Long accountId;

    public WTradingAccountProvider(@Qualifier(WEBULL_REST_TEMPLATE) final RestTemplate restTemplate,
                                   @Value("${providers.webull.trade-account.id}") Long accountId) {
        super(restTemplate);
        this.accountId = accountId;
        log.warn("---------------------------------------------------------------");
        log.warn("---------------$$$ MODE - LIVE TRADING $$$---------------------");
        log.warn("---------------------------------------------------------------");
    }


    @Override
    public Account getAccount() {
        return null;
    }

    @Override
    public List<Order> getOpenOrders() {
        return null;
    }

    @Override
    public List<Order> getOpenOrders(String symbol) {
        return null;
    }

    @Override
    public List<Order> getOrdersHistory(String symbol, Integer daysRange) {
        return null;
    }

    @Override
    public List<Position> getOpenPositions(String symbol) {
        return null;
    }

    @Override
    public List<Position> getOpenPositions() {
        return null;
    }

    @Override
    public TradingRecord getTickerTradingRecord(String symbol, Integer daysRange) {
        return null;
    }

    @Override
    public void placeOrder(OrderRequest orderRequest) {

    }

    @Override
    public void cancelOrder(String id) {

    }
}
