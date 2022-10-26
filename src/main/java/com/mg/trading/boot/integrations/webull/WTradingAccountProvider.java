package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.domain.models.*;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.webull.data.common.WOrder;
import com.mg.trading.boot.integrations.webull.data.common.WOrderStatus;
import com.mg.trading.boot.integrations.webull.data.common.WPosition;
import com.mg.trading.boot.integrations.webull.data.trading.WTAccount;
import com.mg.trading.boot.integrations.webull.data.trading.WTOrderDateType;
import com.mg.trading.boot.integrations.webull.data.trading.WTOrdersQuery;
import com.mg.trading.boot.integrations.webull.data.trading.WTOrdersResult;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;

@Log4j2
@Component
@ConditionalOnProperty(value = "features.paper-trading", havingValue = "false")
public class WTradingAccountProvider extends WAbstractAccountProvider implements AccountProvider {
    private final Long accountId;
    private final WTickerDetailsProvider tickerDetailsProvider;

    public WTradingAccountProvider(@Qualifier(WEBULL_REST_TEMPLATE) final RestTemplate restTemplate,
                                   @Value("${providers.webull.trade-account.id}") Long accountId,
                                   WTickerDetailsProvider tickerDetailsProvider) {
        super(restTemplate);
        this.accountId = accountId;
        this.tickerDetailsProvider = tickerDetailsProvider;
        log.warn("---------------------------------------------------------------");
        log.warn("---------------$$$ MODE - LIVE TRADING $$$---------------------");
        log.warn("---------------------------------------------------------------");
    }

    @Override
    public Account getAccount() {
        WTAccount wtAccount = getWTAccount();

        return mapAccount(wtAccount);
    }

    @Override
    public List<Order> getOpenOrders() {
        String url = WUrls.openOrders(accountId);

        List<WTOrdersResult> response = (List<WTOrdersResult>) get(url, new ParameterizedTypeReference<List<WTOrdersResult>>() {
        }).getBody();

        return Optional.ofNullable(response).orElse(new ArrayList<>()).stream()
                .flatMap(result -> result.getItems().stream())
                .map(WAbstractAccountProvider::mapToOrder).collect(Collectors.toList());
    }

    @Override
    public List<Order> getOpenOrders(String symbol) {
        return getOpenOrders().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getFilledOrdersHistory(String symbol, Integer daysRange) {
        List<WOrder> wOrders = getFilledOrdersHistory(daysRange);
        return sortAsc(filterBySymbol(wOrders, symbol));
    }


    private List<WOrder> getFilledOrdersHistory(Integer daysRange) {
        String url = WUrls.orders(accountId);

        ZonedDateTime date = Instant.now().atZone(BarSeriesUtils.getDefaultZone()).minus(daysRange, ChronoUnit.DAYS);
        String startTime = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);

        WTOrdersQuery query = WTOrdersQuery.builder()
                .secAccountId(String.valueOf(accountId))
                .dateType(WTOrderDateType.ORDER)
                .lastCreateTime0(0L)
                .pageSize(10000)
                .status(WOrderStatus.Filled)
                .startTimeStr(startTime)
                .endTimeStr(null)
                .action(null)
                .build();

        List<WTOrdersResult> response = (List<WTOrdersResult>) post(url, query, new ParameterizedTypeReference<List<WTOrdersResult>>() {
        }).getBody();

        List<WOrder> wOrders = Optional.ofNullable(response).orElse(new ArrayList<>()).stream()
                .flatMap(result -> result.getItems().stream())
                .collect(Collectors.toList());

        return wOrders;
    }

    @Override
    public List<Position> getOpenPositions(String symbol) {
        return getOpenPositions().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<Position> getOpenPositions() {
        return getAccount().getPositions();
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
    public List<TradingLog> getTradingLogs(Integer daysRange) {
        List<WOrder> filledOrders = getFilledOrdersHistory(daysRange);
        Set<String> symbols = filledOrders.stream().map(o -> o.getTicker().getSymbol()).collect(Collectors.toSet());

        return symbols.stream().map(symbol -> getTradingLog(symbol, daysRange)).collect(Collectors.toList());
    }

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Ticker ticker = tickerDetailsProvider.getTicker(orderRequest.getSymbol());
        String url = WUrls.placeOrder(accountId);

        placeOrder(ticker, orderRequest, url);
    }

    @Override
    public void cancelOrder(String id) {
        String serial = generateUUID();
        String url = WUrls.cancelOrder(id, serial, accountId);

        ResponseEntity<?> response = get(url, new ParameterizedTypeReference<String>() {
        });

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Unexpected status code on order cancellation: " + response.getStatusCode());
        }
    }

    @Override
    public void updateOrder(OrderRequest orderRequest) {
        String url = WUrls.updateOrder(accountId);
        Ticker ticker = tickerDetailsProvider.getTicker(orderRequest.getSymbol());

        placeOrder(ticker, orderRequest, url);
    }


    private WTAccount getWTAccount() {
        String url = WUrls.accountSummary(accountId);
        WTAccount wtAccount = (WTAccount) get(url, new ParameterizedTypeReference<WTAccount>() {
        }).getBody();

        checkNotNull(wtAccount, "WTAccount should not be null");
        return wtAccount;
    }

    private List<Position> getAccountPositions(WTAccount wtAccount) {
        List<WPosition> positions = wtAccount.getAssetSummaryVO().getPositions();

        return positions.stream().map(WAbstractAccountProvider::mapToPosition).collect(Collectors.toList());
    }

    private Account mapAccount(WTAccount wtAccount) {
        return Account.builder()
                .id(String.valueOf(wtAccount.getAccountSummaryVO().getSecAccountId()))
                .currency(wtAccount.getAccountSummaryVO().getCurrency())
                .totalMarketValue(wtAccount.getAccountSummaryVO().getTotalMarketValue())
                .longMarketValue(wtAccount.getAccountSummaryVO().getLongMarketValue())
                .shortMarketValue(wtAccount.getAccountSummaryVO().getShortMarketValue())
                .totalCash(wtAccount.getAccountSummaryVO().getTotalCashValue())
                .buyingPower(wtAccount.getAccountSummaryVO().getDayBuyingPower())
                .settledFunds(wtAccount.getAccountSummaryVO().getSettledFunds())
                .unsettledFunds(wtAccount.getAccountSummaryVO().getUnsettledFunds())
                .positions(getAccountPositions(wtAccount))
                .openOrders(getOpenOrders())
                .build();
    }
}
