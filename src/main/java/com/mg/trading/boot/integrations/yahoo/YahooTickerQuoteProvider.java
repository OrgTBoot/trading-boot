package com.mg.trading.boot.integrations.yahoo;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.integrations.yahoo.data.*;
import com.mg.trading.boot.models.Interval;
import com.mg.trading.boot.models.Range;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.utils.NumberUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mg.trading.boot.config.BeanConfig.YAHOO_REST_TEMPLATE;
import static com.mg.trading.boot.integrations.yahoo.YahooTickerQuoteProvider.YAHOO_QUOTE_PROVIDER;
import static com.mg.trading.boot.utils.NumberUtils.defaultToZero;
import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
@Component
@Qualifier(YAHOO_QUOTE_PROVIDER)
public class YahooTickerQuoteProvider extends AbstractRestProvider implements TickerQuoteProvider {
    public final static String YAHOO_QUOTE_PROVIDER = "yahoo_quote_provider";
    private final String baseUrl = "https://query1.finance.yahoo.com";

    public YahooTickerQuoteProvider(@Qualifier(YAHOO_REST_TEMPLATE) RestTemplate restTemplate) {
        super(restTemplate);
        this.restTemplate = restTemplate;
    }

    @Override
    public List<TickerQuote> getTickerQuotes(String symbol, Range range, Interval interval) {
        String quoteInterval = interval.value + interval.unit;
        String quoteRange = range.value + range.unit;
        String url = String.format(baseUrl + "/v8/finance/chart/%s?interval=%s&range=%s", symbol, quoteInterval,
                quoteRange);

        ParameterizedTypeReference<YahooTickerQuotes> type = new ParameterizedTypeReference<YahooTickerQuotes>() {
        };
        YahooTickerQuotes response = (YahooTickerQuotes) super.get(url, type).getBody();

        List<TickerQuote> quotes = mapToTickerQuotes(response);

        log.info("YO Extracted {} quotes for range={}, interval={}, url={}", quotes.size(), range, interval, url);

        return quotes;
    }

    private List<TickerQuote> mapToTickerQuotes(YahooTickerQuotes yahooTickerQuotes) {
        List<TickerQuote> tickerQuotes = new ArrayList<>();

        YahooChart chart = yahooTickerQuotes.getChart();
        if (chart != null && !CollectionUtils.isEmpty(chart.getResult())) {
            YahooChartResult result = chart.getResult().get(0);
            YahooChartResultMeta metadata = result.getMeta();
            YahooChartResultIndicatorsQuote quotes = result.getIndicators().getQuote().get(0);
            List<Long> timeStamps = Optional.ofNullable(result.getTimestamp()).orElse(new ArrayList<>());

            for (int i = 0; i <= timeStamps.size() - 1; i++) {
                TickerQuote tickerQuote = TickerQuote.builder()
                        .timeZone(metadata.getExchangeTimezoneName())
                        .timeStamp(result.getTimestamp().get(i))
                        .openPrice(toRndBigDecimal(defaultToZero(quotes.getOpen().get(i))))
                        .closePrice(toRndBigDecimal(defaultToZero(quotes.getClose().get(i))))
                        .highPrice(toRndBigDecimal(defaultToZero(quotes.getHigh().get(i))))
                        .lowPrice(toRndBigDecimal(defaultToZero(quotes.getLow().get(i))))
                        .volume(defaultToZero(quotes.getVolume().get(i)))
                        .build();
                tickerQuotes.add(tickerQuote);
            }

        }
        return tickerQuotes;
    }

}
