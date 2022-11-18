package com.mg.trading.boot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.domain.executors.StrategyExecutor;
import com.mg.trading.boot.domain.executors.StrategiesCacheManager;
import com.mg.trading.boot.domain.models.Ticker;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.models.TradingLog;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.Parameters;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.domain.strategy.crypto.st1.CryptoStrategyDefinitionV8;
import com.mg.trading.boot.domain.strategy.dema1.DEMAStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.DEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.strategy.dema3.DEMAStrategyDefinitionV3;
import com.mg.trading.boot.domain.strategy.dema4.DEMAStrategyDefinitionV4;
import com.mg.trading.boot.domain.strategy.dema5.DEMAStrategyDefinitionV5;
import com.mg.trading.boot.domain.strategy.dema6.DEMAStrategyDefinitionV6;
import com.mg.trading.boot.domain.strategy.dema7.DEMAStrategyDefinitionV7;
import com.mg.trading.boot.domain.strategy.dema8.DEMAStrategyDefinitionV8;
import com.mg.trading.boot.domain.strategy.dema9.DEMAStrategyDefinitionV9;
import com.mg.trading.boot.domain.strategy.ema.EMAStrategyDefinition;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.logging.LogPackage;
import com.mg.trading.boot.logging.LogsManagementService;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.util.List;
import java.util.Set;

import static org.ta4j.core.Trade.TradeType.BUY;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider broker;
    private final ScreenerProvider screener;
    private final StrategyExecutor strategyExecutor;
    private final LogsManagementService logsManagementService;
    private final StrategiesCacheManager strategiesCacheManager;

    public GQLController(final LogsManagementService logsManagementService, final BrokerProvider broker, final ScreenerProvider screener, final StrategiesCacheManager strategiesCacheManager, final StrategyExecutor strategyExecutor) {
        this.broker = broker;
        this.screener = screener;
        this.strategyExecutor = strategyExecutor;
        this.logsManagementService = logsManagementService;
        this.strategiesCacheManager = strategiesCacheManager;
    }

    @GraphQLQuery(description = "Run Screening with a predefined criteria.")
    public List<Ticker> fetchScreenedTickers() {
        return screener.getUnusualVolume();
    }


    @GraphQLQuery(description = "Print trading records for a given symbol.")
    public String fetchTradingRecords(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol, @GraphQLArgument(name = "daysRange") @GraphQLNonNull final Integer daysRange) {

        TradingLog tradingLog = broker.account().getTradingLog(symbol, daysRange);
        ReportGenerator.printTradingRecords(tradingLog);
        ReportGenerator.printTradingSummary(tradingLog);

        return "Completed. Please see details in console.";
    }

    @GraphQLQuery(description = "Print trading records for all traded symbols.")
    public String fetchAllTradingRecords(@GraphQLArgument(name = "daysRange") @GraphQLNonNull final Integer daysRange) {

        List<TradingLog> tradingLogs = broker.account().getTradingLogs(daysRange);
        tradingLogs.forEach(tradingLog -> {
            ReportGenerator.printTradingRecords(tradingLog);
            ReportGenerator.printTradingSummary(tradingLog);
        });

        return "Completed. Please see details in console.";
    }

    @GraphQLQuery(description = "Returns a list keys for all the running strategies.")
    public Set<String> fetchRunningStrategyKeys() {
        return strategyExecutor.getRunningStrategyKeys();
    }

    @GraphQLMutation
    public String triggerSymbolsBackTracking(@GraphQLArgument(name = "symbols") @GraphQLNonNull final List<String> symbols, @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name) throws Exception {
        for (String symbol : symbols) {
            triggerBackTracking(symbol, name);
        }
        return "Completed. Please see details in console.";
    }

    @GraphQLMutation
    public String triggerBackTracking(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol, @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name) throws Exception {

        StrategyDefinition strategyDef = selectStrategyDef(name, symbol);
        Parameters params = strategyDef.getParams();

        List<TickerQuote> quotes = broker.ticker().getTickerQuotes(symbol, params.getQuotesRange(), params.getQuotesInterval());
        log.info("{} Quotes: {}", symbol, new ObjectMapper().writeValueAsString(quotes));
        strategyDef.updateSeries(quotes); // load series in to strategy

        BarSeriesManager seriesManager = new BarSeriesManager(strategyDef.getSeries());
        TradingRecord tradingRecord = seriesManager.run(strategyDef.getStrategy(), BUY, DecimalNum.valueOf(1));

        ReportGenerator.printTradingRecords(tradingRecord, symbol);
        ReportGenerator.printTradingSummary(tradingRecord, symbol);
        return "Completed. Please see details in console.";
    }

    @GraphQLMutation(description = "Start trading strategy for the given symbol. Strategy will run in background until stopped.")
    public String triggerLiveTrading(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol, @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name) {

        log.info("Initializing {} strategy for {}...", name, symbol);

        StrategyDefinition strategyDef = selectStrategyDef(name, symbol);
        if (strategiesCacheManager.contains(strategyDef)) {
            return "This strategy is already running. Running keys: " + strategiesCacheManager.getRunningKeys();
        }
        strategyExecutor.start(strategyDef);

        return "Strategy is running...";
    }

    @GraphQLMutation(description = "Stops trading strategy execution.")
    public String triggerLiveTradingStop(@GraphQLArgument(name = "strategyKey") @GraphQLNonNull final String strategyKey) {

        strategyExecutor.stop(strategyKey);
        return "Strategy removed " + strategyKey;
    }

    @GraphQLMutation(description = "Change log level")
    public String triggerLogLevelChange(@GraphQLArgument(name = "level") @GraphQLNonNull final LogLevel level, @GraphQLArgument(name = "package") @GraphQLNonNull final LogPackage logPackage) {

        logsManagementService.updateLogLevel(logPackage, level);
        return "Log level updated to  " + level;
    }


    private StrategyDefinition selectStrategyDef(TradingStrategies name, String symbol) {
        switch (name) {
            case EMA:
                return new EMAStrategyDefinition(symbol);
            case DEMA:
                return new DEMAStrategyDefinition(symbol);
            case DEMA_V2:
                return new DEMAStrategyDefinitionV2(symbol);
            case DEMA_V3:
                return new DEMAStrategyDefinitionV3(symbol);
            case DEMA_V4:
                return new DEMAStrategyDefinitionV4(symbol);
            case DEMA_V5:
                return new DEMAStrategyDefinitionV5(symbol);
            case DEMA_V6:
                return new DEMAStrategyDefinitionV6(symbol);
            case DEMA_V7:
                return new DEMAStrategyDefinitionV7(symbol);
            case DEMA_V8:
                return new DEMAStrategyDefinitionV8(symbol);
            case DEMA_V9:
                return new DEMAStrategyDefinitionV9(symbol);
            case CRYPTO_V8:
                return new CryptoStrategyDefinitionV8(symbol);
            default:
                throw new ValidationException("Strategy not supported");
        }
    }
}
