package com.mg.trading.boot.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.exceptions.ValidationException;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.models.StrategyContext;
import com.mg.trading.boot.models.Ticker;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.StrategyExecutor;
import com.mg.trading.boot.strategy.TradingStrategyExecutor;
import com.mg.trading.boot.strategy.core.StrategyParameters;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.core.StrategySeriesInitializer;
import com.mg.trading.boot.strategy.dema.v1.DEMAStrategyProvider;
import com.mg.trading.boot.strategy.dema.v2.DEMAStrategyProviderV2;
import com.mg.trading.boot.strategy.ema.EMAStrategyProvider;
import com.mg.trading.boot.strategy.reporting.ReportGenerator;
import com.mg.trading.boot.utils.BarSeriesUtils;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.rules.BooleanRule;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static com.mg.trading.boot.utils.NumberUtils.toDecimalNum;
import static org.ta4j.core.Trade.TradeType.BUY;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider brokerProvider;
    private final ScreenerProvider screeningProvider;


    public GQLController(final BrokerProvider brokerProvider,
                         final ScreenerProvider screeningProvider) {
        this.brokerProvider = brokerProvider;
        this.screeningProvider = screeningProvider;
    }

    @GraphQLQuery(description = "Run Screening with a predefined criteria.")
    public List<Ticker> fetchScreenedTickers() {
        return screeningProvider.getUnusualVolume();
    }


    @GraphQLQuery(description = "Print trading records for a given symbol.")
    public String fetchTradingRecords(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
                                      @GraphQLArgument(name = "daysRange") @GraphQLNonNull final Integer daysRange) {

        BooleanRule dummyRule = new BooleanRule(false);
        BaseBarSeries dummySeries = new BaseBarSeries();
        BaseStrategy dummyStrategy = new BaseStrategy("UNKNOWN STRATEGY (REPORTING)", dummyRule, dummyRule);
        ReportGenerator reportGenerator = new ReportGenerator(symbol, dummyStrategy, dummySeries);

        TradingRecord tradingRecord = brokerProvider.account().getTickerTradingRecord(symbol, daysRange);
        reportGenerator.printTradingRecords(tradingRecord);
        reportGenerator.printTradingSummary(tradingRecord);

        return "Completed. Please see details in console.";
    }

    @GraphQLQuery(description = "Returns a list keys for all the running strategies.")
    public Set<String> fetchRunningStrategyKeys() {
        return RunningStrategiesHolder.getRunningKeys();
    }

    @GraphQLMutation
    public String triggerBackTracking(
            @GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
            @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name,
            @GraphQLArgument(name = "sharesQty", defaultValue = "1") final BigDecimal sharesQty) throws JsonProcessingException {

        StrategyProvider strategyProvider = selectStrategy(name, symbol, sharesQty);
        StrategyParameters parameters = strategyProvider.getParameters();

        List<TickerQuote> quotes = brokerProvider.ticker().getTickerQuotes(
                parameters.getSymbol(),
                parameters.getQuotesRange(),
                parameters.getQuotesInterval());

        log.info("Quotes: {}", new ObjectMapper().writeValueAsString(quotes));

        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(parameters.getQuotesInterval().seconds));

        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy, BUY, toDecimalNum(parameters.getSharesQty()));

        ReportGenerator reporting = new ReportGenerator(parameters.getSymbol(), strategy, series);
        reporting.printTradingRecords(tradingRecord);
        reporting.printTradingSummary(tradingRecord);

        return "Completed. Please see details in console.";
    }

    @GraphQLMutation(description = "Start trading strategy for the given symbol. Strategy will run in background until stopped.")
    public String triggerLiveTrading(
            @GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
            @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name,
            @GraphQLArgument(name = "sharesQty", defaultValue = "1") final BigDecimal sharesQty) {

        log.info("Initializing {} strategy for {}...", name, symbol);
        final StrategyProvider strategyProvider = selectStrategy(name, symbol, sharesQty);
        final StrategyParameters params = strategyProvider.getParameters();
        final BarSeries series = StrategySeriesInitializer.init(brokerProvider, params);
        final Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();

        StrategyContext context = StrategyContext.builder()
                .broker(brokerProvider)
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
    public String triggerLiveTradingStop(
            @GraphQLArgument(name = "strategyKey") @GraphQLNonNull final String strategyKey) {

        RunningStrategiesHolder.remove(strategyKey);
        return "Strategy removed " + strategyKey;
    }

//    @GraphQLQuery
//    public TickerSentiment findTickerSentiment(
//            @GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol,
//            @GraphQLArgument(name = "daysRange") @GraphQLNonNull final Integer daysRange) {
//        return brokerProvider.getTickerSentimentByNews(symbol, daysRange);
//    }

    private StrategyProvider selectStrategy(TradingStrategies name, String symbol, BigDecimal sharesQty) {
        switch (name) {
            case EMA:
                return new EMAStrategyProvider(symbol, sharesQty);
            case DEMA:
                return new DEMAStrategyProvider(symbol, sharesQty);
            case DEMA_V2:
                return new DEMAStrategyProviderV2(symbol, sharesQty);
            default:
                throw new ValidationException("Strategy not supported");
        }
    }
}
