package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.TickerDetailsProvider;
import com.mg.trading.boot.integrations.webull.data.common.WTicker;
import com.mg.trading.boot.integrations.webull.data.common.WTickerData;
import com.mg.trading.boot.integrations.webull.data.common.WTickerQuote;
import com.mg.trading.boot.domain.models.Interval;
import com.mg.trading.boot.domain.models.Range;
import com.mg.trading.boot.domain.models.Ticker;
import com.mg.trading.boot.domain.models.TickerQuote;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static com.mg.trading.boot.config.BeanConfig.WEBULL_REST_TEMPLATE;
import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;

@Log4j2
@Component
public class WTickerDetailsProvider extends AbstractRestProvider implements TickerDetailsProvider {

    public WTickerDetailsProvider(@Qualifier(WEBULL_REST_TEMPLATE) RestTemplate restTemplate) {
        super(restTemplate);
        this.restTemplate = restTemplate;
    }

    @Override
    public Ticker getTicker(String symbol) {
        WTicker wTicker = this.getTickerBySymbol(symbol);

        if (wTicker == null) {
            throw new ValidationException("Ticker not found :" + symbol);
        }

        return mapToTicker(wTicker);
    }

    @Override
    public List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval) {

        Ticker ticker = getTicker(symbol);
        String url;
        switch (interval) {
            case ONE_MINUTE:
                url = isCrypto(ticker) ? WUrls.cryptoQuotes(ticker.getExternalId(), interval) : WUrls.quotesByMinute(ticker.getExternalId(), range);
                break;

            default:
                throw new RuntimeException("Unsupported interval. Consider adding it.: " + interval);
        }

        ParameterizedTypeReference<List<WTickerQuote>> type = new ParameterizedTypeReference<List<WTickerQuote>>() {
        };
        List<WTickerQuote> wbQuotes = (List<WTickerQuote>) get(url, type).getBody();

        List<TickerQuote> quotes = new ArrayList<>();
        if (wbQuotes != null) {
            wbQuotes.forEach(it -> quotes.addAll(toTickerQuotes(it)));
        }

        log.debug("WB Extracted {} quotes for range={}, interval={}, url={}", quotes.size(), range, interval, url);

        return quotes.stream()
                .sorted(Comparator.comparing(TickerQuote::getTimeStamp))
                .collect(Collectors.toList());
    }

    @Override
    public TickerQuote getLatestTickerQuote(String symbol) {
        List<TickerQuote> tickerQuotes = getTickerQuotes(symbol, Range.ONE_DAY, Interval.ONE_MINUTE);
        return tickerQuotes.get(tickerQuotes.size() - 1);
    }

    private List<TickerQuote> toTickerQuotes(WTickerQuote quotes) {
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

    private WTicker getTickerBySymbol(String symbol) {
        String url = WUrls.ticker(symbol);
        ParameterizedTypeReference<WTickerData> type = new ParameterizedTypeReference<WTickerData>() {
        };
        ResponseEntity<WTickerData> response = (ResponseEntity<WTickerData>) get(url, type);

        List<WTicker> data = Optional.ofNullable(response.getBody()).map(WTickerData::getData).orElse(null);
        final WTicker ticker = CollectionUtils.isEmpty(data) ? null : data.get(0);

        if (ticker != null) {
            checkState(symbol.equalsIgnoreCase(ticker.getSymbol()), "Unexpected ticker extracted.");
        }
        return ticker;
    }

    public static Ticker mapToTicker(WTicker ticker) {
        if (ticker == null) {
            return null;
        }

        return Ticker.builder()
                .externalId(String.valueOf(ticker.getTickerId()))
                .symbol(ticker.getSymbol())
                .company(ticker.getName())
                .assetType(ticker.getTemplate())
                .build();
    }

    private boolean isCrypto(Ticker ticker) {
        return ticker.getAssetType().equalsIgnoreCase("crypto");
    }
}
