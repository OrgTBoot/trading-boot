package com.mg.trading.boot.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.domain.exceptions.ValidationException;
import com.mg.trading.boot.domain.executors.IStrategyExecutor;
import com.mg.trading.boot.domain.strategy.IParameters;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema.XDEMAStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.XDEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.strategy.ema.XEMAStrategyDefinition;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.domain.models.Ticker;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.models.TradingLog;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.dema.v1.DEMAStrategyProvider;
import com.mg.trading.boot.strategy.dema.v2.DEMAStrategyProviderV2;
import com.mg.trading.boot.strategy.ema.EMAStrategyProvider;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;

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

    public GQLController(final BrokerProvider broker, final ScreenerProvider screener, final IStrategyExecutor strategyExecutor) {
        this.broker = broker;
        this.screener = screener;
        this.strategyExecutor = strategyExecutor;
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
