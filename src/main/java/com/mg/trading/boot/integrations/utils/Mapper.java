package com.mg.trading.boot.integrations.utils;

import com.mg.trading.boot.data.*;
import com.mg.trading.boot.exceptions.ModelMappingException;
import com.mg.trading.boot.integrations.webull.data.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class Mapper {

    public static Order toOrder(WbOrder wbOrder) {
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
                .ticker(toTicker(wbOrder.getTicker()))
                .build();
    }

    public static Position toPosition(WbPosition position) {
        return Position.builder()
                .id(String.valueOf(position.getId()))
                .accountId(String.valueOf(position.getAccountId()))
                .cost(position.getCost())
                .costPrice(position.getCostPrice())
                .lastPrice(position.getLastPrice())
                .quantity(position.getPosition())
                .marketValue(position.getMarketValue())
                .ticker(toTicker(position.getTicker()))
                .build();
    }

    public static Account toAccount(WbAccount account) {
        return Account.builder()
                .openOrders(account.getOpenOrders().stream().map(Mapper::toOrder).collect(Collectors.toList()))
                .positions(account.getPositions().stream().map(Mapper::toPosition).collect(Collectors.toList()))
                .build();
    }

    public static Ticker toTicker(WbTicker ticker) {
        if (ticker == null) {
            return null;
        }

        return Ticker.builder()
                .externalId(String.valueOf(ticker.getTickerId()))
                .symbol(ticker.getSymbol())
                .company(ticker.getName())
                .build();
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

    public static List<Ticker> toTickers(List<Map<String, String>> listOfFinVizMaps) {
        return listOfFinVizMaps.stream().map(it ->
                        Ticker.builder()
                                .externalId(it.get("No."))
                                .symbol(it.get("Ticker"))
                                .company(it.get("Company"))
                                .industry(it.get("Industry"))
                                .sector(it.get("Sector"))
                                .country(it.get("Country"))
                                .marketCap(it.get("Market Cap"))
                                .peRatio(toBigDecimal(it.get("P/E")))
                                .price(toBigDecimal(it.get("Price")))
                                .change(toBigDecimal(it.get("Change")))
                                .volume(toBigDecimal(it.get("Volume")))
                                .build())
                .collect(Collectors.toList());
    }

    /**
     * EX: 1663881900,18.36,18.34,18.36,18.34,18.34,1381,18.35
     */
    public static List<TickerQuote> toTickerQuotes(WbTickerQuote quotes) {
        return quotes.getData().stream().map(it -> {
                    String[] values = it.split(",");
                    Long timestamp = Long.parseLong(values[0]);
                    BigDecimal open = toBigDecimal(values[1]);
                    BigDecimal close = toBigDecimal(values[2]);
                    BigDecimal high = toBigDecimal(values[3]);
                    BigDecimal low = toBigDecimal(values[4]);
                    Long volume = toBigDecimal(values[6]).longValue();

                    return TickerQuote.builder()
                            .timeZone(quotes.getTimeZone())
                            .timeStamp(timestamp)
                            .openPrice(open)
                            .closePrice(close)
                            .highPrice(high)
                            .lowPrice(low)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static BigDecimal toBigDecimal(Num num) {
        return BigDecimal.valueOf(num.floatValue()).round(new MathContext(3, RoundingMode.CEILING));
    }

    private static BigDecimal toBigDecimal(String value) {
        value = normalizeNumber(value);
        if (StringUtils.isEmpty(value)) {
            return BigDecimal.ZERO;
        }

        try {
            return BigDecimal.valueOf(Double.parseDouble(value));
        } catch (Exception e) {
            log.error("Failed to convert value '{}' to BigDecimal", value);
            throw new ModelMappingException(e);
        }
    }

    private static String normalizeNumber(String value) {
        return value.toLowerCase().replaceAll("[^0-9.]+", "");
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

}
