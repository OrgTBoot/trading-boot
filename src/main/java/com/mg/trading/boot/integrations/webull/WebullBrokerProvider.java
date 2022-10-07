package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.analisys.SentimentAnalysisService;
import com.mg.trading.boot.exceptions.ValidationException;
import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.webull.data.*;
import com.mg.trading.boot.models.*;
import com.mg.trading.boot.models.npl.TickerSentiment;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;
import static com.mg.trading.boot.integrations.webull.WebullBrokerProvider.REST_WEBULL_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_BY_NAME;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_NEWS;
import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;

@Log4j2
@Component
@Qualifier(REST_WEBULL_PROVIDER)
public class WebullBrokerProvider extends AbstractRestProvider implements BrokerProvider {
    public static final String REST_WEBULL_PROVIDER = "webull";
    private final Long accountId;

    public WebullBrokerProvider(@Qualifier(WEBULL_REST_TEMPLATE) final RestTemplate restTemplate,
                                @Value("${providers.webull.account.id}") Long accountId) {
        super(restTemplate);
        this.accountId = accountId;
    }

    @Override
    public Ticker getTicker(String symbol) {
        WbTicker wbTicker = this.getTickerBySymbol(symbol);

        if (wbTicker == null) {
            throw new ValidationException("Ticker not found :" + symbol);
        }

        return mapToTicker(wbTicker);
    }

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Ticker ticker = getTicker(orderRequest.getSymbol());
        Long tickerId = Long.valueOf(ticker.getExternalId());

        WbOrderRequest wbOrderRequest = WbOrderRequest.builder()
                .tickerId(tickerId)
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

//        log.info("Placing order: {}", wbOrderRequest);
        String url = String.format(WebullEndpoints.PAPER_PLACE_ORDER.value, accountId, tickerId);
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
        String url = String.format(WebullEndpoints.PAPER_CANCEL_ORDER.value, accountId, id);
        final ParameterizedTypeReference<String> type = new ParameterizedTypeReference<String>() {
        };
        post(url, null, type);
    }

    @Override
    public List<Order> getOpenOrders() {
        return Optional.ofNullable(getAccount().getOpenOrders()).orElse(new ArrayList<>());
    }

    @Override
    public List<Order> getOpenOrdersBySymbol(String symbol) {
        return getOpenOrders().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<Position> getPositions() {
        return Optional.ofNullable(getAccount().getPositions()).orElse(new ArrayList<>());
    }

    @Override
    public List<Position> getPositionsBySymbol(String symbol) {
        return getPositions().stream().filter(it -> Objects.equals(it.getTicker().getSymbol(), symbol))
                .collect(Collectors.toList());
    }

    @Override
    public List<TickerNewsArticle> getTickerNews(String symbol, Long daysAgoRelevance) {
        WbTicker ticker = getTickerBySymbol(symbol);
        if (ticker == null) {
            return new ArrayList<>();
        }

        String pageSize = "100";
        Long id = ticker.getTickerId();
        String url = String.format(TICKER_NEWS.value, id, pageSize);
        ParameterizedTypeReference<List<WbNewsArticle>> type = new ParameterizedTypeReference<List<WbNewsArticle>>() {
        };
        ResponseEntity<List<WbNewsArticle>> response = (ResponseEntity<List<WbNewsArticle>>) get(url, type);

        return toNewsArticles(response.getBody()).stream()
                .filter(it -> it.getNewsDaysAgo() <= daysAgoRelevance)
                .collect(Collectors.toList());
    }

    @Override
    public TickerSentiment getTickerSentimentByNews(String symbol, Long daysAgoRelevance) {
        List<TickerNewsArticle> news = getTickerNews(symbol, daysAgoRelevance);
        final SentimentAnalysisService analysisService = new SentimentAnalysisService();
        return analysisService.getSentimentByTickerArticles(news);
    }

    private WbTicker getTickerBySymbol(String symbol) {
        String url = String.format(TICKER_BY_NAME.value, symbol);
        ParameterizedTypeReference<WbTickerData> type = new ParameterizedTypeReference<WbTickerData>() {
        };
        ResponseEntity<WbTickerData> response = (ResponseEntity<WbTickerData>) get(url, type);

        List<WbTicker> data = Optional.ofNullable(response.getBody()).map(WbTickerData::getData).orElse(null);
        return CollectionUtils.isEmpty(data) ? null : data.get(0);
    }

    private Account getAccount() {
        String url = String.format(WebullEndpoints.PAPER_ACCOUNT.value, accountId);
        ParameterizedTypeReference<WbAccount> type = new ParameterizedTypeReference<WbAccount>() {
        };
        ResponseEntity<WbAccount> response = (ResponseEntity<WbAccount>) get(url, type);

        return mapToAccount(response.getBody());
    }

//-----------------------------------------------------
//----------------Private Methods----------------------
//-----------------------------------------------------

    public static Order mapToOrder(WbOrder wbOrder) {
        return Order.builder()
                .id(String.valueOf(wbOrder.getOrderId()))
                .orderType(mapWbOrderType(wbOrder.getOrderType()))
                .action(mapWbOrderAction(wbOrder.getAction()))
                .timeInForce(mapWbOrderTimeInForce(wbOrder.getTimeInForce()))
                .status(mapWbOrderStatus(wbOrder.getStatus()))
                .placedTime(wbOrder.getPlacedTime())
                .lmtPrice(toBigDecimal(wbOrder.getLmtPrice()))
                .filledQuantity(toBigDecimal(wbOrder.getFilledQuantity()))
                .totalQuantity(toBigDecimal(wbOrder.getTotalQuantity()))
                .ticker(mapToTicker(wbOrder.getTicker()))
                .build();
    }

    public static Position maoToPosition(WbPosition position) {
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

    public static Account mapToAccount(WbAccount account) {
        return Account.builder()
                .openOrders(account.getOpenOrders().stream().map(WebullBrokerProvider::mapToOrder).collect(Collectors.toList()))
                .positions(account.getPositions().stream().map(WebullBrokerProvider::maoToPosition).collect(Collectors.toList()))
                .build();
    }

    public static Ticker mapToTicker(WbTicker ticker) {
        if (ticker == null) {
            return null;
        }

        return Ticker.builder()
                .externalId(String.valueOf(ticker.getTickerId()))
                .symbol(ticker.getSymbol())
                .company(ticker.getName())
                .build();
    }

    public static WbOrderTimeInForce mapWbOrderTimeInForce(OrderTimeInForce timeInForce) {
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

    public static WbOrderType mapWbOrderType(OrderType type) {
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

    public static WbOrderAction mapWbOrderAction(OrderAction action) {
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

    public static OrderTimeInForce mapWbOrderTimeInForce(WbOrderTimeInForce timeInForce) {
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

    public static OrderType mapWbOrderType(WbOrderType type) {
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

    public static OrderAction mapWbOrderAction(WbOrderAction action) {
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

    private static OrderStatus mapWbOrderStatus(WbOrderStatus status) {
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


    public static List<TickerNewsArticle> toNewsArticles(List<WbNewsArticle> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return new ArrayList<>();
        }

        return articles.stream().map(it ->
                        TickerNewsArticle.builder()
                                .externalId(String.valueOf(it.getId()))
                                .title(it.getTitle())
                                .newsUrl(it.getNewsUrl())
                                .sourceName(it.getSourceName())
                                .newsTime(it.getNewsTime())
                                .newsDaysAgo(ChronoUnit.DAYS.between(it.getNewsTime(), Instant.now()))
                                .build())
                .collect(Collectors.toList());
    }

}
