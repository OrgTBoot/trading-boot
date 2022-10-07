package com.mg.trading.boot.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.models.StrategyContext;
import com.mg.trading.boot.models.Ticker;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.models.npl.TickerSentiment;
import com.mg.trading.boot.strategy.StrategyExecutor;
import com.mg.trading.boot.strategy.TradingStrategyExecutor;
import com.mg.trading.boot.strategy.core.*;
import com.mg.trading.boot.strategy.goldencross.DEMAStrategyProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import com.mg.trading.boot.utils.ConsoleUtils;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mg.trading.boot.integrations.finviz.RestFinvizProvider.REST_FINVIZ_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullBrokerProvider.REST_WEBULL_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullTickerQuoteProvider.WEBULL_QUOTE_PROVIDER;
import static com.mg.trading.boot.integrations.yahoo.YahooTickerQuoteProvider.YAHOO_QUOTE_PROVIDER;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider brokerProvider;
    private final TickerQuoteProvider tickerQuoteProvider;
    private final ScreenerProvider screeningProvider;
    private final Map<String, StrategyExecutor> runningStrategiesMap = new HashMap<>();


    public GQLController(@Qualifier(REST_WEBULL_PROVIDER) final BrokerProvider brokerProvider,
                         @Qualifier(REST_FINVIZ_PROVIDER) final ScreenerProvider screeningProvider,
                         @Qualifier(WEBULL_QUOTE_PROVIDER) final TickerQuoteProvider tickerQuoteProvider) {
//                         @Qualifier(YAHOO_QUOTE_PROVIDER) final TickerQuoteProvider tickerQuoteProvider) {
        this.brokerProvider = brokerProvider;
        this.screeningProvider = screeningProvider;
        this.tickerQuoteProvider = tickerQuoteProvider;
    }

//    @GraphQLQuery
//    public TickerSentiment findTickerSentiment(
//            @GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
//            @GraphQLArgument(name = "daysAgoRelevance") @GraphQLNonNull final Long daysAgoRelevance) {
//        return brokerProvider.getTickerSentimentByNews(symbol, daysAgoRelevance);
//    }

    @SneakyThrows
    @GraphQLQuery
    public String runBackTrackingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol) {
        DEMAStrategyProvider strategyProvider = new DEMAStrategyProvider(symbol);
        StrategyParameters parameters = strategyProvider.getParameters();

        List<TickerQuote> quotes = this.tickerQuoteProvider.getTickerQuotes(
                parameters.getSymbol(),
                parameters.getQuotesRange(),
                parameters.getQuotesInterval());

        log.info("Quotes: {}", new ObjectMapper().writeValueAsString(quotes));

        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(parameters.getQuotesInterval().seconds));

        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy);

        ConsoleUtils.printTradingRecords(symbol, tradingRecord);
        ConsoleUtils.printTradingStatement(symbol, strategy, tradingRecord, series);

        return "OK";
    }

    @GraphQLQuery
    public List<Ticker> runScreening() {
        return this.screeningProvider.getUnusualVolume();
    }

    @GraphQLMutation
    public String startTradingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol) {
        log.info("Initializing strategy...");
        final DEMAStrategyProvider strategyProvider = new DEMAStrategyProvider(symbol);
        final StrategyParameters params = strategyProvider.getParameters();

        final BarSeries series = StrategySeriesInitializer.init(tickerQuoteProvider, params);
        final TradingRecord tradingRecord = StrategyTradingRecordInitializer.init(brokerProvider, params);
        final Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        final TickerQuoteExtractor quoteListener = StrategyTickerListenerInitializer.init(tickerQuoteProvider,
                brokerProvider, params,
                strategy, series, tradingRecord);

        StrategyContext context = StrategyContext.builder()
                .tradingRecord(tradingRecord)
                .parameters(params)
                .strategy(strategy)
                .series(series)
                .quoteExtractor(quoteListener)
                .build();

        StrategyExecutor strategyExecutor = new TradingStrategyExecutor(context);

        String key = symbol + "_" + strategyExecutor.getClass().getSimpleName();
        if (!this.runningStrategiesMap.containsKey(key)) {
            strategyExecutor.start();
            this.runningStrategiesMap.put(key, strategyExecutor);
            return "Strategy is running: " + key;
        } else {
            return "This strategy is already running. Key: " + key;
        }
    }

    @GraphQLMutation
    public String stopTradingStrategy(@GraphQLArgument(name = "name") @GraphQLNonNull final String name) {
        StrategyExecutor strategyExecutor = this.runningStrategiesMap.get(name);
        if (strategyExecutor != null) {
            strategyExecutor.stop();
            this.runningStrategiesMap.remove(name);
        }
        return "Strategy is stopped " + name;
    }

    @GraphQLQuery
    public Set<String> showRunningStrategies() {
        return this.runningStrategiesMap.keySet();
    }

}
