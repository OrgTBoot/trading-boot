package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.webull.data.common.*;
import com.mg.trading.boot.integrations.webull.data.paper.WPAccount;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;

@Log4j2
@Component
@ConditionalOnProperty(value = "features.paper-trading", havingValue = "true")
public class WPaperAccountProvider extends WAbstractAccountProvider implements AccountProvider {
    private final Long accountId;
    private final WTickerDetailsProvider tickerDetailsProvider;

    public WPaperAccountProvider(@Qualifier(WEBULL_REST_TEMPLATE) final RestTemplate restTemplate,
                                 @Value("${providers.webull.paper-account.id}") Long paperAccountId,
                                 WTickerDetailsProvider tickerDetailsProvider) {
        super(restTemplate);
        this.accountId = paperAccountId;
        this.tickerDetailsProvider = tickerDetailsProvider;
        log.warn("---------------------------------------------------------------");
        log.warn("-------------------MODE - PAPER TRADING------------------------");
        log.warn("---------------------------------------------------------------");
    }

    @Override
    public Account getAccount() {
        String url = WUrls.paperAccount(accountId);
        ParameterizedTypeReference<WPAccount> type = new ParameterizedTypeReference<WPAccount>() {
        };
        ResponseEntity<WPAccount> response = (ResponseEntity<WPAccount>) get(url, type);

        return mapToAccount(response.getBody());
    }

    @Override
    public List<Order> getOpenOrders() {
        return Optional.ofNullable(getAccount().getOpenOrders()).orElse(new ArrayList<>());
    }

    @Override
    public List<Order> getOpenOrders(String symbol) {
        return getOpenOrders().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getFilledOrdersHistory(String symbol, Integer daysRange) {
        ZonedDateTime date = Instant.now().atZone(BarSeriesUtils.getDefaultZone()).minus(daysRange, ChronoUnit.DAYS);
        String url = WUrls.paperFilledOrders(accountId, date);
        ParameterizedTypeReference<List<WOrder>> type = new ParameterizedTypeReference<List<WOrder>>() {
        };
        List<WOrder> response = (List<WOrder>) get(url, type).getBody();
        List<WOrder> wOrders = Optional.ofNullable(response).orElse(new ArrayList<>());

        return super.filterBySymbolSortAsc(wOrders, symbol);
    }

    @Override
    public List<Position> getOpenPositions(String symbol) {
        return getOpenPositions().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<Position> getOpenPositions() {
        return Optional.ofNullable(getAccount().getPositions()).orElse(new ArrayList<>());
    }

    @Override
    public TradingLog getTradingLog(String symbol, Integer daysRange) {
        return TradingLog.builder()
                .symbol(symbol)
                .daysRange(daysRange)
                .filledOrders(getFilledOrdersHistory(symbol, daysRange))
                .openOrders(getOpenOrders(symbol))
                .positions(getOpenPositions(symbol))
                .build();
    }

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Ticker ticker = tickerDetailsProvider.getTicker(orderRequest.getSymbol());
        String url = WUrls.paperPlaceOrder(accountId, Long.valueOf(ticker.getExternalId()));

        placeOrder(ticker, orderRequest, url);
    }

    @Override
    public void cancelOrder(String id) {
        log.info("Canceling order: {}", id);
        String url = WUrls.paperCancelOrder(accountId, id);

        post(url, null, new ParameterizedTypeReference<String>() {
        });
    }


    private static Account mapToAccount(WPAccount account) {
        return Account.builder()
                .openOrders(account.getOpenOrders().stream().map(WAbstractAccountProvider::mapToOrder).collect(Collectors.toList()))
                .positions(account.getPositions().stream().map(WAbstractAccountProvider::mapToPosition).collect(Collectors.toList()))
                .build();
    }

}
