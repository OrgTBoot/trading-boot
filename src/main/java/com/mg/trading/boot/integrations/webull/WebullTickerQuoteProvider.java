package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.integrations.webull.data.WbTickerQuote;
import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import com.mg.trading.boot.models.Ticker;
import com.mg.trading.boot.models.TickerQuote;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_MINUTE_QUOTES;
import static com.mg.trading.boot.integrations.webull.WebullEndpoints.TICKER_QUOTES;
import static com.mg.trading.boot.integrations.webull.WebullTickerQuoteProvider.WEBULL_QUOTE_PROVIDER;
import static com.mg.trading.boot.models.TradingPeriod.EXTENDED;
import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;

@Log4j2
@Component
@Qualifier(WEBULL_QUOTE_PROVIDER)
public class WebullTickerQuoteProvider extends AbstractRestProvider implements TickerQuoteProvider {
    public static final String WEBULL_QUOTE_PROVIDER = "webull_quote_provider";
    private WebullBrokerProvider webullBrokerProvider;

    public WebullTickerQuoteProvider(@Qualifier(WEBULL_REST_TEMPLATE) RestTemplate restTemplate,
                                     WebullBrokerProvider webullBrokerProvider) {
        super(restTemplate);
        this.restTemplate = restTemplate;
        this.webullBrokerProvider = webullBrokerProvider;
    }

    @Override
    public List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval) {

        Ticker ticker = webullBrokerProvider.getTicker(symbol);
        String id = ticker.getExternalId();
        String rangeValue = range.unit + range.value;
//        String quoteType = interval.unit + interval.value;
//        String extendedTrading = EXTENDED.value;
//        Long limit = range.seconds / interval.seconds;
//        String url = String.format(TICKER_QUOTES.value, id, quoteType, extendedTrading, limit);

//todo: there are few different API that provide different data for same params. Use this one for now but consider
// splitting and adding other ranges
        String url;
        switch (interval) {
            case ONE_MINUTE:
                url = String.format(TICKER_MINUTE_QUOTES.value, id, rangeValue);
                break;

            default:
                throw new RuntimeException("Unsupported interval. Consider adding it.: " + interval);
        }

        ParameterizedTypeReference<List<WbTickerQuote>> type = new ParameterizedTypeReference<List<WbTickerQuote>>() {
        };
        List<WbTickerQuote> wbQuotes = (List<WbTickerQuote>) get(url, type).getBody();

        List<TickerQuote> quotes = new ArrayList<>();
        if (wbQuotes != null) {
            wbQuotes.forEach(it -> quotes.addAll(toTickerQuotes(it)));
        }

        log.info("WB Extracted {} quotes for range={}, interval={}, url={}", quotes.size(), range, interval, url);

        return quotes.stream()
                .sorted(Comparator.comparing(TickerQuote::getTimeStamp))
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
}
