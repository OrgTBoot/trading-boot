package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.webull.data.*;
import com.mg.trading.boot.models.*;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;
import static com.mg.trading.boot.integrations.webull.WTickerDetailsProvider.mapToTicker;
import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;
import static com.mg.trading.boot.utils.NumberUtils.toDecimalNum;

@Log4j2
@Component
@ConditionalOnProperty(value = "features.paper-trading", havingValue = "true")
public class WPaperAccountProvider extends AbstractRestProvider implements AccountProvider {
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
        ParameterizedTypeReference<WbAccount> type = new ParameterizedTypeReference<WbAccount>() {
        };
        ResponseEntity<WbAccount> response = (ResponseEntity<WbAccount>) get(url, type);

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
    public List<Order> getOrdersHistory(String symbol, Integer daysRange) {
        ZonedDateTime date = Instant.now().atZone(BarSeriesUtils.getDefaultZone()).minus(daysRange, ChronoUnit.DAYS);
        String url = WUrls.paperFilledOrders(accountId, date);
        ParameterizedTypeReference<List<WbOrder>> type = new ParameterizedTypeReference<List<WbOrder>>() {
        };
        List<WbOrder> response = (List<WbOrder>) get(url, type).getBody();

        List<Order> orders = Optional.ofNullable(response)
                .orElse(new ArrayList<>()).stream()
                .filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .map(this::mapToOrder)
                .sorted(Comparator.comparing(Order::getFilledTime))// sort ascending
                .collect(Collectors.toList());

        return orders;
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
    public TradingRecord getTickerTradingRecord(String symbol, Integer daysRange) {
        List<Order> orders = getOrdersHistory(symbol, daysRange);
        log.debug("Extracted {} historical orders", orders.size());

        TradingRecord tradingRecord = new BaseTradingRecord();
        AtomicInteger index = new AtomicInteger();
        orders.forEach(order -> {
            int idx = index.getAndIncrement();
            Num price = toDecimalNum(order.getAvgFilledPrice());
            Num qty = toDecimalNum(order.getFilledQuantity());

            tradingRecord.operate(idx, price, qty);
        });
        return tradingRecord;
    }

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Ticker ticker = tickerDetailsProvider.getTicker(orderRequest.getSymbol());
        Long tickerID = Long.valueOf(ticker.getExternalId());
        WbOrderRequest wbOrderRequest = WbOrderRequest.builder()
                .tickerId(tickerID)
                .action(mapWbOrderAction(orderRequest.getAction()))
                .orderType(mapWbOrderType(orderRequest.getOrderType()))
                .timeInForce(mapWbOrderTimeInForce(orderRequest.getTimeInForce()))
                .lmtPrice(orderRequest.getLmtPrice())
                .quantity(orderRequest.getQuantity())
                .outsideRegularTradingHour(Boolean.TRUE)
                .shortSupport(Boolean.TRUE)
                .comboType(WbOrderComboType.NORMAL)
                .serialId(UUID.randomUUID().toString())
                .build();

        log.info("Placing order: {}", wbOrderRequest);
        String url = WUrls.paperPlaceOrder(accountId, tickerID);
        ParameterizedTypeReference<WbOrder> type = new ParameterizedTypeReference<WbOrder>() {
        };
        ResponseEntity<WbOrder> response = (ResponseEntity<WbOrder>) post(url, wbOrderRequest, type);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Unexpected status code on order placement: " + response.getStatusCode());
        }

        if (response.getBody() == null) {
            throw new RuntimeException("Unexpected response body on order placement: " + response.getBody());
        }
    }

    @Override
    public void cancelOrder(String id) {
        log.info("Canceling order: {}", id);
        String url = WUrls.paperCancelOrder(accountId, id);

        post(url, null, new ParameterizedTypeReference<String>() {
        });
    }


    private Account mapToAccount(WbAccount account) {
        return Account.builder()
                .openOrders(account.getOpenOrders().stream().map(this::mapToOrder).collect(Collectors.toList()))
                .positions(account.getPositions().stream().map(this::maoToPosition).collect(Collectors.toList()))
                .build();
    }

    private Order mapToOrder(WbOrder wbOrder) {
        return Order.builder()
                .id(String.valueOf(wbOrder.getOrderId()))
                .orderType(mapWbOrderType(wbOrder.getOrderType()))
                .action(mapWbOrderAction(wbOrder.getAction()))
                .timeInForce(mapWbOrderTimeInForce(wbOrder.getTimeInForce()))
                .status(mapWbOrderStatus(wbOrder.getStatus()))
                .filledTime(wbOrder.getFilledTime0())
                .placedTime(wbOrder.getPlacedTime())
                .lmtPrice(toBigDecimal(wbOrder.getLmtPrice()))
                .avgFilledPrice(toBigDecimal(wbOrder.getAvgFilledPrice()))
                .filledQuantity(toBigDecimal(wbOrder.getFilledQuantity()))
                .totalQuantity(toBigDecimal(wbOrder.getTotalQuantity()))
                .ticker(mapToTicker(wbOrder.getTicker()))
                .build();
    }

    private Position maoToPosition(WbPosition position) {
        return Position.builder()
                .id(String.valueOf(position.getId()))
                .accountId(String.valueOf(position.getAccountId()))
                .cost(position.getCost())
                .costPrice(position.getCostPrice())
                .lastPrice(position.getLastPrice())
                .quantity(position.getPosition())
                .marketValue(position.getMarketValue())
                .ticker(mapToTicker(position.getTicker()))
                .build();
    }

    private WbOrderType mapWbOrderType(OrderType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case LIMIT:
                return WbOrderType.LMT;
            case MARKET:
                return WbOrderType.MKT;
            default:
                throw new RuntimeException("Could not map order type: " + type);
        }
    }

    private WbOrderAction mapWbOrderAction(OrderAction action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case BUY:
                return WbOrderAction.BUY;
            case SELL:
                return WbOrderAction.SELL;
            default:
                throw new RuntimeException("Could not map action: " + action);
        }
    }

    private OrderTimeInForce mapWbOrderTimeInForce(WbOrderTimeInForce timeInForce) {
        if (timeInForce == null) {
            return null;
        }
        switch (timeInForce) {
            case DAY:
                return OrderTimeInForce.DAY;
            case GTC:
                return OrderTimeInForce.GTC;
            default:
                throw new RuntimeException("Could not map order time in force: " + timeInForce);
        }
    }

    private WbOrderTimeInForce mapWbOrderTimeInForce(OrderTimeInForce timeInForce) {
        if (timeInForce == null) {
            return null;
        }
        switch (timeInForce) {
            case DAY:
                return WbOrderTimeInForce.DAY;
            case GTC:
                return WbOrderTimeInForce.GTC;
            default:
                throw new RuntimeException("Could not map order time in force: " + timeInForce);
        }
    }

    private OrderType mapWbOrderType(WbOrderType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case LMT:
                return OrderType.LIMIT;
            case MKT:
                return OrderType.MARKET;
            default:
                throw new RuntimeException("Could not map order type: " + type);
        }
    }

    private OrderAction mapWbOrderAction(WbOrderAction action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case BUY:
                return OrderAction.BUY;
            case SELL:
                return OrderAction.SELL;
            default:
                throw new RuntimeException("Could not map action: " + action);
        }
    }

    private OrderStatus mapWbOrderStatus(WbOrderStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case Cancelled:
                return OrderStatus.CANCELED;
            case Working:
                return OrderStatus.WORKING;
            case Filled:
                return OrderStatus.FILLED;
            case Failed:
                return OrderStatus.FAILED;
            default:
                throw new RuntimeException("Could not map status: " + status);
        }
    }
}
