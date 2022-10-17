package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.webull.data.common.*;
import com.mg.trading.boot.integrations.webull.data.paper.WPOrderRequest;
import com.mg.trading.boot.models.*;
import com.mg.trading.boot.utils.TradingRecordUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mg.trading.boot.integrations.webull.WTickerDetailsProvider.mapToTicker;
import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;
import static com.mg.trading.boot.utils.NumberUtils.toDecimalNum;

@Log4j2
public abstract class WAbstractAccountProvider extends AbstractRestProvider {

    public WAbstractAccountProvider(RestTemplate restTemplate) {
        super(restTemplate);
    }

    protected TradingRecord getTickerTradingRecord(List<Order> orders) {
        List<Order> aggOrders = TradingRecordUtils.aggregateOrders(orders); // covers BUY, BUY, SEL scenarios

        if (orders.size() != aggOrders.size()) {
            log.warn("There was a need to aggregate some of the orders, was {}, became {}", orders.size(), aggOrders.size());
        }

        TradingRecord tradingRecord = new BaseTradingRecord();
        AtomicInteger index = new AtomicInteger();
        aggOrders.forEach(order -> {
            int idx = index.getAndIncrement();
            Num price = toDecimalNum(order.getAvgFilledPrice());
            Num qty = toDecimalNum(order.getFilledQuantity());

            tradingRecord.operate(idx, price, qty);
        });
        return tradingRecord;
    }

    public void placeOrder(Ticker ticker, OrderRequest orderRequest, String url) {
        Long tickerID = Long.valueOf(ticker.getExternalId());
        WPOrderRequest wpOrderRequest = WPOrderRequest.builder()
                .tickerId(tickerID)
                .action(mapOrderAction(orderRequest.getAction()))
                .orderType(mapOrderType(orderRequest.getOrderType()))
                .timeInForce(mapOrderTimeInForce(orderRequest.getTimeInForce()))
                .lmtPrice(orderRequest.getLmtPrice())
                .quantity(orderRequest.getQuantity())
                .outsideRegularTradingHour(Boolean.TRUE)
                .shortSupport(Boolean.TRUE)
                .comboType(WOrderComboType.NORMAL)
                .serialId(generateUUID())
                // specific for non-paper trading crypto scenario
//                .entrustType(WTEntrustType.QTY)
//                .assetType(ticker.getAssetType())
                .build();

        log.info("Placing order: {}", wpOrderRequest);
        ParameterizedTypeReference<WOrder> type = new ParameterizedTypeReference<WOrder>() {
        };
        ResponseEntity<WOrder> response = (ResponseEntity<WOrder>) post(url, wpOrderRequest, type);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Unexpected status code on order placement: " + response.getStatusCode());
        }

        if (response.getBody() == null) {
            throw new RuntimeException("Unexpected response body on order placement: " + response.getBody());
        }
    }


    protected static Order mapToOrder(WOrder wOrder) {
        WOrderStatus wStatus = Optional.ofNullable(wOrder.getStatus()).orElse(wOrder.getStatusCode());
        String limitPrice = Optional.ofNullable(wOrder.getLmtPrice()).orElse(wOrder.getAvgFilledPrice());

        return Order.builder()
                .id(String.valueOf(wOrder.getOrderId()))
                .orderType(mapWbOrderType(wOrder.getOrderType()))
                .action(mapWbOrderAction(wOrder.getAction()))
                .timeInForce(mapWbOrderTimeInForce(wOrder.getTimeInForce()))
                .status(mapWbOrderStatus(wStatus))
                .filledTime(wOrder.getFilledTime0())
                .placedTime(wOrder.getPlacedTime())
                .lmtPrice(toBigDecimal(limitPrice))
                .avgFilledPrice(toBigDecimal(wOrder.getAvgFilledPrice()))
                .filledQuantity(toBigDecimal(wOrder.getFilledQuantity()))
                .totalQuantity(toBigDecimal(wOrder.getTotalQuantity()))
                .ticker(mapToTicker(wOrder.getTicker()))
                .build();
    }

    protected static OrderType mapWbOrderType(WOrderType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case LMT:
                return OrderType.LIMIT;
            case MKT:
                return OrderType.MARKET;
            case STP:
                return OrderType.STRAIGHT_THROUGH_PROCESSING;
            default:
                throw new RuntimeException("Could not map order type: " + type);
        }
    }

    protected static OrderAction mapWbOrderAction(WOrderAction action) {
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

    protected static OrderStatus mapWbOrderStatus(WOrderStatus status) {
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

    protected static OrderTimeInForce mapWbOrderTimeInForce(WOrderTimeInForce timeInForce) {
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

    private static WOrderType mapOrderType(OrderType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case LIMIT:
                return WOrderType.LMT;
            case MARKET:
                return WOrderType.MKT;
            case STRAIGHT_THROUGH_PROCESSING:
                return WOrderType.STP;
            default:
                throw new RuntimeException("Could not map order type: " + type);
        }
    }

    private static WOrderAction mapOrderAction(OrderAction action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case BUY:
                return WOrderAction.BUY;
            case SELL:
                return WOrderAction.SELL;
            default:
                throw new RuntimeException("Could not map action: " + action);
        }
    }

    private static WOrderTimeInForce mapOrderTimeInForce(OrderTimeInForce timeInForce) {
        if (timeInForce == null) {
            return null;
        }
        switch (timeInForce) {
            case DAY:
                return WOrderTimeInForce.DAY;
            case GTC:
                return WOrderTimeInForce.GTC;
            default:
                throw new RuntimeException("Could not map order time in force: " + timeInForce);
        }
    }


    protected static Position mapToPosition(WPosition position) {
        BigDecimal quantity = Optional.ofNullable(position.getQuantity())
                .orElse(position.getPosition());//position is for paper trading
        return Position.builder()
                .id(String.valueOf(position.getId()))
                .cost(position.getCost())
                .costPrice(position.getCostPrice())
                .lastPrice(position.getLastPrice())
                .quantity(quantity)
                .marketValue(position.getMarketValue())
                .ticker(mapToTicker(position.getTicker()))
                .build();
    }

    protected String generateUUID() {
        return UUID.randomUUID().toString();
    }

    protected List<Order> filterBySymbolSortAsc(List<WOrder> orders, String symbol) {
        return orders.stream()
                .filter(it -> StringUtils.equalsIgnoreCase(it.getTicker().getSymbol(), symbol))
                .map(WAbstractAccountProvider::mapToOrder)
                .sorted(Comparator.comparing(Order::getFilledTime))// sort ascending
                .collect(Collectors.toList());
    }

}
