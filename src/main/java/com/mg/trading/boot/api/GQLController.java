package com.mg.trading.boot.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.domain.executors.IStrategyExecutor;
import com.mg.trading.boot.domain.models.Ticker;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.models.TradingLog;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.IParameters;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema.XDEMAStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.XDEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.strategy.ema.XEMAStrategyDefinition;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.mg.trading.boot.utils.NumberUtils.toDecimalNum;
import static org.ta4j.core.Trade.TradeType.BUY;

@Log4j2
@Service
@GraphQLApi
public class GQLController {
    private final BrokerProvider broker;
    private final ScreenerProvider screener;
    private final IStrategyExecutor strategyExecutor;
    private final LogsManagementService logsManagementService;

    public GQLController(final LogsManagementService logsManagementService, final BrokerProvider broker, final ScreenerProvider screener, final IStrategyExecutor strategyExecutor) {
        this.broker = broker;
        this.screener = screener;
        this.strategyExecutor = strategyExecutor;
        this.logsManagementService = logsManagementService;
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

    @GraphQLQuery(description = "Returns a list keys for all the running strategies.")
    public Set<String> fetchRunningStrategyKeys() {
        return strategyExecutor.getRunningStrategyKeys();
    }

    @GraphQLMutation
    public String triggerBackTracking(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol, @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name, @GraphQLArgument(name = "sharesQty", defaultValue = "1") final BigDecimal sharesQty) throws JsonProcessingException {

        IStrategyDefinition strategyDef = selectStrategyDef(name, symbol);
        IParameters params = strategyDef.getParams();

        List<TickerQuote> quotes = broker.ticker().getTickerQuotes(symbol, params.getQuotesRange(), params.getQuotesInterval());
        log.info("Quotes: {}", new ObjectMapper().writeValueAsString(quotes));
        strategyDef.updateSeries(quotes); // load series in to strategy

        BarSeriesManager seriesManager = new BarSeriesManager(strategyDef.getSeries());
        TradingRecord tradingRecord = seriesManager.run(strategyDef.getStrategy(), BUY, toDecimalNum(sharesQty));

        ReportGenerator.printTradingRecords(tradingRecord, symbol);
        ReportGenerator.printTradingSummary(tradingRecord, symbol);
        return "Completed. Please see details in console.";
    }

    @GraphQLMutation(description = "Start trading strategy for the given symbol. Strategy will run in background until stopped.")
    public String triggerLiveTrading(@GraphQLArgument(name = "symbol") @GraphQLNonNull final String symbol, @GraphQLArgument(name = "strategy") @GraphQLNonNull final TradingStrategies name, @GraphQLArgument(name = "sharesQty", defaultValue = "1") final BigDecimal sharesQty) {

        log.info("Initializing {} strategy for {}...", name, symbol);
        IStrategyDefinition strategyDef = selectStrategyDef(name, symbol);
        strategyExecutor.start(strategyDef);

        return "Strategy is running...";
    }

    @GraphQLMutation(description = "Stops trading strategy execution.")
    public String triggerLiveTradingStop(@GraphQLArgument(name = "strategyKey") @GraphQLNonNull final String strategyKey) {

        strategyExecutor.stop(strategyKey);
        return "Strategy removed " + strategyKey;
    }

    @GraphQLMutation(description = "Change log level")
    public String triggerLogLevelChange(@GraphQLArgument(name = "level")
                                        @GraphQLNonNull final LogLevel level,
                                        @GraphQLArgument(name = "package")
                                        @GraphQLNonNull final LogPackage logPackage) {

        logsManagementService.updateLogLevel(logPackage, level);
        return "Log level updated to  " + level;
    }


    private IStrategyDefinition selectStrategyDef(TradingStrategies name, String symbol) {
        switch (name) {
            case EMA:
                return new XEMAStrategyDefinition(symbol);
            case DEMA:
                return new XDEMAStrategyDefinition(symbol);
            case DEMA_V2:
                return new XDEMAStrategyDefinitionV2(symbol);
            default:
                throw new ValidationException("Strategy not supported");
        }
    }
}
