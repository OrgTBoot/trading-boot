package com.mg.trading.boot.graphql;


import com.mg.trading.boot.data.*;
import com.mg.trading.boot.data.npl.TickerSentiment;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreeningProvider;
import com.mg.trading.boot.strategy.*;
import com.mg.trading.boot.strategy.core.StrategySeriesInitializer;
import com.mg.trading.boot.strategy.core.StrategyTickerListenerInitializer;
import com.mg.trading.boot.strategy.core.StrategyTradingRecordInitializer;
import com.mg.trading.boot.strategy.goldencross.GoldenCrossStrategyInitializer;
import com.mg.trading.boot.strategy.goldencross.GoldenCrossStrategyParameters;
import com.mg.trading.boot.strategy.core.TickerQuoteExtractor;
import com.mg.trading.boot.utils.BarSeriesUtils;
import com.mg.trading.boot.utils.TradingRecordUtils;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
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
import static com.mg.trading.boot.integrations.webull.RestWebullProvider.REST_WEBULL_PROVIDER;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider brokerProvider;
    private final ScreeningProvider screeningProvider;
    private final Map<String, StrategyExecutor> runningStrategiesMap = new HashMap<>();


    public GQLController(@Qualifier(REST_WEBULL_PROVIDER) final BrokerProvider brokerProvider,
                         @Qualifier(REST_FINVIZ_PROVIDER) final ScreeningProvider screeningProvider) {
        this.brokerProvider = brokerProvider;
        this.screeningProvider = screeningProvider;
    }

    @GraphQLQuery
    public TickerSentiment findTickerSentiment(
            @GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
            @GraphQLArgument(name = "daysAgoRelevance") @GraphQLNonNull final Long daysAgoRelevance) {
        return brokerProvider.getTickerSentimentByNews(symbol, daysAgoRelevance);
    }

    @GraphQLQuery
    public TradingMetrics runBackTrackingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol) {
        GoldenCrossStrategyParameters parameters = GoldenCrossStrategyInitializer.params(symbol).toBuilder()
                .quotesRollingLimit(360)//6 HOURS of quotes aggregated by minute
                .build();

        List<TickerQuote> quotes = this.brokerProvider.getTickerQuotes(
                parameters.getSymbol(),
                parameters.getQuotesInterval(),
                parameters.getTradingPeriod(),
                parameters.getQuotesRollingLimit());

        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        Strategy strategy = GoldenCrossStrategyInitializer.init(parameters, series);
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy);
        TradingMetrics tradingMetrics = TradingRecordUtils.buildTradingMetrics(symbol, series, tradingRecord);

        TradingRecordUtils.printTradingRecords(symbol, tradingRecord);
        TradingRecordUtils.printTradingMetrics(tradingMetrics);

        return tradingMetrics;
    }

    @GraphQLQuery
    public List<Ticker> runScreening() {
        return this.screeningProvider.getTopGaines();
    }

    @GraphQLQuery
    public Set<String> showRunningStrategies() {
        return this.runningStrategiesMap.keySet();
    }


    @GraphQLMutation
    public String startTradingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol) {
        log.info("Initializing strategy...");
        final GoldenCrossStrategyParameters params = GoldenCrossStrategyInitializer.params(symbol);
        final BarSeries series = StrategySeriesInitializer.init(brokerProvider, params);
        final TradingRecord tradingRecord = StrategyTradingRecordInitializer.init(brokerProvider, params);
        final Strategy strategy = GoldenCrossStrategyInitializer.init(params, series);
        final TickerQuoteExtractor quoteListener = StrategyTickerListenerInitializer.init(brokerProvider, params, strategy, series, tradingRecord);

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

}
