package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.data.*;
import com.mg.trading.boot.data.npl.TickerSentiment;
import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.utils.Mapper;
import com.mg.trading.boot.integrations.webull.data.*;
import com.mg.trading.boot.analisys.SentimentAnalysisService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;
import static com.mg.trading.boot.integrations.webull.RestWebullProvider.REST_WEBULL_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_BY_NAME;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_NEWS;

@Log4j2
@Service
@Qualifier(REST_WEBULL_PROVIDER)
public class RestWebullProvider extends AbstractRestProvider implements BrokerProvider {
    public static final String REST_WEBULL_PROVIDER = "webull";
    private final Long accountId;

    public RestWebullProvider(@Qualifier(WEBULL_REST_TEMPLATE) final RestTemplate restTemplate,
                              @Value("${providers.webull.account.id}") Long accountId) {
        super(restTemplate);
        this.accountId = accountId;
    }

    @Override
    public Ticker getTicker(String symbol) {
        WbTicker wbTicker = this.getTickerBySymbol(symbol);

        return Mapper.toTicker(wbTicker);
    }

    @Override
    public List<TickerQuote> getTickerQuotes(String symbol,
                                             Interval interval,
                                             TradingPeriod tradingPeriod,
                                             Integer limit) {
        Ticker ticker = getTicker(symbol);
        String url = String.format(WebullEndpoints.TICKER_QUOTES.value, ticker.getExternalId(), interval.value, tradingPeriod.value, limit);
        ParameterizedTypeReference<List<WbTickerQuote>> type = new ParameterizedTypeReference<List<WbTickerQuote>>() {
        };
        List<WbTickerQuote> wbQuotes = (List<WbTickerQuote>) get(url, type).getBody();

        List<TickerQuote> quotes = new ArrayList<>();
        if (wbQuotes != null) {
            wbQuotes.forEach(it -> quotes.addAll(Mapper.toTickerQuotes(it)));
        }

        return quotes.stream()
                .sorted(Comparator.comparing(TickerQuote::getTimeStamp))
                .collect(Collectors.toList());
    }


    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Ticker ticker = getTicker(orderRequest.getSymbol());
        Long tickerId = Long.valueOf(ticker.getExternalId());

        WbOrderRequest wbOrderRequest = WbOrderRequest.builder()
                .tickerId(tickerId)
                .action(Mapper.mapWbOrderAction(orderRequest.getAction()))
                .orderType(Mapper.mapWbOrderType(orderRequest.getOrderType()))
                .timeInForce(Mapper.mapWbOrderTimeInForce(orderRequest.getTimeInForce()))
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

        return Mapper.toNewsArticles(response.getBody()).stream()
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

        return Mapper.toAccount(response.getBody());
    }

//    private void removeLastQuoteIfIncomplete(List<TickerQuote> quotes) {
//        final TickerQuote lastQuote = quotes.get(quotes.size() - 1);
//        final TickerQuote secondLastQuote = quotes.get(quotes.size() - 2);
//        final TickerQuote thirdLastQuote = quotes.get(quotes.size() - 3);
//        if (lastQuote.getTimeStamp() - secondLastQuote.getTimeStamp() < secondLastQuote.getTimeStamp() - thirdLastQuote.getTimeStamp()) {
//            log.info("Removing last quote - not complete");
//            quotes.remove(quotes.size() - 1);
//        }
//    }
}
