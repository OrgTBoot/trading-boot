package com.mg.trading.boot.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.exceptions.ValidationException;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.integrations.TickerQuoteProvider;
import com.mg.trading.boot.models.StrategyContext;
import com.mg.trading.boot.models.Ticker;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.StrategyExecutor;
import com.mg.trading.boot.strategy.TradingStrategyExecutor;
import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.core.StrategySeriesInitializer;
import com.mg.trading.boot.strategy.dema.DEMAStrategyProvider;
import com.mg.trading.boot.strategy.ema.EMAStrategyProvider;
import com.mg.trading.boot.strategy.reporting.TradingReportGenerator;
import com.mg.trading.boot.utils.BarSeriesUtils;
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
import java.util.List;
import java.util.Set;

import static com.mg.trading.boot.integrations.finviz.RestFinvizProvider.REST_FINVIZ_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullBrokerProvider.REST_WEBULL_PROVIDER;
import static com.mg.trading.boot.integrations.webull.WebullTickerQuoteProvider.WEBULL_QUOTE_PROVIDER;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider brokerProvider;
    private final TickerQuoteProvider tickerQuoteProvider;
    private final ScreenerProvider screeningProvider;


    public GQLController(@Qualifier(REST_WEBULL_PROVIDER) final BrokerProvider brokerProvider,
                         @Qualifier(REST_FINVIZ_PROVIDER) final ScreenerProvider screeningProvider,
                         @Qualifier(WEBULL_QUOTE_PROVIDER) final TickerQuoteProvider tickerQuoteProvider) {
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
    public String runBackTrackingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
                                          @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name) {
        StrategyProvider strategyProvider = selectStrategy(name, symbol);
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

        TradingReportGenerator reporting = new TradingReportGenerator(symbol, strategy);
        reporting.printTradingRecords(tradingRecord);
        reporting.printTradingSummary(tradingRecord, series);

        return "Completed. Please see details in console.";
    }

    @GraphQLQuery(description = "Run Screening with a predefined criteria.")
    public List<Ticker> runScreening() {
        return this.screeningProvider.getUnusualVolume();
    }

    @GraphQLMutation(description = "Start trading strategy for the given symbol.")
    public String startTradingStrategy(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
                                       @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name) {
        log.info("Initializing {} strategy for {}...", name, symbol);
        final StrategyProvider strategyProvider = selectStrategy(name, symbol);
        final StrategyParameters params = strategyProvider.getParameters();
        final BarSeries series = StrategySeriesInitializer.init(tickerQuoteProvider, params);
        final Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();

        StrategyContext context = StrategyContext.builder()
                .broker(brokerProvider)
                .quoteProvider(tickerQuoteProvider)
                .parameters(params)
                .strategy(strategy)
                .series(series)
                .build();

        StrategyExecutor strategyExecutor = new TradingStrategyExecutor(context);
        RunningStrategiesHolder.add(strategyExecutor);

        strategyExecutor.start();

        return "Strategy is running...";
    }

    @GraphQLMutation(description = "Stops trading strategy execution.")
    public String stopTradingStrategy(@GraphQLArgument(name = "strategyKey") @GraphQLNonNull final String strategyKey) {
        RunningStrategiesHolder.remove(strategyKey);
        return "Strategy removed " + strategyKey;
    }

    @GraphQLQuery(description = "Returns a list keys for all the running strategies.")
    public Set<String> getRunningStrategyKeys() {
        return RunningStrategiesHolder.getRunningKeys();
    }


    private StrategyProvider selectStrategy(TradingStrategies name, String symbol) {
        switch (name) {
            case EMA:
                return new EMAStrategyProvider(symbol);
            case DEMA:
                return new DEMAStrategyProvider(symbol);
            default:
                throw new ValidationException("Strategy not supported");
        }
    }
}
