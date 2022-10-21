package com.mg.trading.boot;

import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.XDEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.ta4j.core.*;

import java.util.List;


@Log4j2
public class IStrategyDefinitionTest {

    @Test
    public void testMultipleStocks() {
        String symbol = "OPEN";
        List<TickerQuote> quotes = TestDataProvider.getQuotesFromFile(symbol + ".json");
//        BarSeries series = new BaseBarSeries();
//        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        IStrategyDefinition dema = new XDEMAStrategyDefinitionV2(symbol);
        BarSeries series = dema.updateSeries(quotes);

        Strategy strategy = dema.getStrategy();

//        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);
//
        TradingRecord tradingRecord = seriesManager.run(strategy);
//
//        log.info(strategyProvider.getClass().getSimpleName());
        ReportGenerator.printTradingRecords(tradingRecord, symbol);
        ReportGenerator.printTradingSummary(tradingRecord, symbol);
//
//        return ReportGenerator.buildTradingStatement(tradingRecord);
    }

}
