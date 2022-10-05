package com.mg.trading.boot;

import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.models.TradingMetrics;
import com.mg.trading.boot.strategy.goldencross.DEMAStrategyProvider;
import com.mg.trading.boot.strategy.goldencross.EMAStrategyProvider;
import com.mg.trading.boot.strategy.goldencross.StrategyProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import com.mg.trading.boot.utils.ConsoleUtils;
import com.mg.trading.boot.utils.TradingRecordUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.*;

import java.time.Duration;
import java.util.List;

@Log4j2
public class StrategyTest {

    @Test
    public void testGainScenario_INVT() {
        String symbol = "IMVT";
        TradingMetrics emaMetrics = testStrategy(symbol, new EMAStrategyProvider(symbol));

        Assert.assertEquals(1.0, emaMetrics.getWinningPositionsRatio().doubleValue(), 0);
        Assert.assertEquals(4.88, emaMetrics.getTotalPercentReturn().doubleValue(), 0);
        Assert.assertEquals(2, emaMetrics.getTotalPositions(), 0);
        Assert.assertEquals(0.401, emaMetrics.getTotalReturn().doubleValue(), 0);
        Assert.assertEquals(2, emaMetrics.getTotalWinningPositions().doubleValue(), 0);
        Assert.assertEquals(0, emaMetrics.getTotalLoosingPositions().doubleValue(), 0);


        TradingMetrics demaMetrics = testStrategy(symbol, new DEMAStrategyProvider(symbol));

        Assert.assertEquals(1.0, demaMetrics.getWinningPositionsRatio().doubleValue(), 0);
        Assert.assertEquals(9.01, demaMetrics.getTotalPercentReturn().doubleValue(), 0);
        Assert.assertEquals(3, demaMetrics.getTotalPositions(), 0);
        Assert.assertEquals(0.731, demaMetrics.getTotalReturn().doubleValue(), 0);
        Assert.assertEquals(3, demaMetrics.getTotalWinningPositions().doubleValue(), 0);
        Assert.assertEquals(0, demaMetrics.getTotalLoosingPositions().doubleValue(), 0);

        assertFirstTradingMetricBeatsSecond(demaMetrics, emaMetrics);
    }

    @Test
    public void testGainScenario_LADR() {
        String symbol = "LADR";
        TradingMetrics emaMetrics = testStrategy(symbol, new EMAStrategyProvider(symbol));
        TradingMetrics demaMetrics = testStrategy(symbol, new DEMAStrategyProvider(symbol));
        assertFirstTradingMetricBeatsSecond(demaMetrics, emaMetrics);
    }


    @Test
    public void testGainScenario_OPEN() {
        String symbol = "OPEN";
        TradingMetrics emaMetrics = testStrategy(symbol, new EMAStrategyProvider(symbol));

        Assert.assertEquals(0.5, emaMetrics.getWinningPositionsRatio().doubleValue(), 0);
        Assert.assertEquals(-1.48, emaMetrics.getTotalPercentReturn().doubleValue(), 0);
        Assert.assertEquals(2, emaMetrics.getTotalPositions(), 0);
        Assert.assertEquals(-0.0509, emaMetrics.getTotalReturn().doubleValue(), 0);
        Assert.assertEquals(1, emaMetrics.getTotalWinningPositions().doubleValue(), 0);
        Assert.assertEquals(1, emaMetrics.getTotalLoosingPositions().doubleValue(), 0);


        TradingMetrics demaMetrics = testStrategy(symbol, new DEMAStrategyProvider(symbol));

        Assert.assertEquals(0.5, demaMetrics.getWinningPositionsRatio().doubleValue(), 0);
        Assert.assertEquals(-1.33, demaMetrics.getTotalPercentReturn().doubleValue(), 0);
        Assert.assertEquals(2, demaMetrics.getTotalPositions(), 0);
        Assert.assertEquals(-0.046, demaMetrics.getTotalReturn().doubleValue(), 0);
        Assert.assertEquals(1, demaMetrics.getTotalWinningPositions().doubleValue(), 0);
        Assert.assertEquals(1, demaMetrics.getTotalLoosingPositions().doubleValue(), 0);

        assertFirstTradingMetricBeatsSecond(demaMetrics, emaMetrics);
    }

    void assertFirstTradingMetricBeatsSecond(TradingMetrics first, TradingMetrics second) {
        Assert.assertTrue(first.getTotalPercentReturn().doubleValue() >= second.getTotalPercentReturn().doubleValue());
        Assert.assertTrue(first.getTotalReturn().doubleValue() >= second.getTotalReturn().doubleValue());
        Assert.assertTrue(first.getTotalPositions() >= second.getTotalPositions());
        Assert.assertTrue(first.getWinningPositionsRatio().doubleValue() >= second.getWinningPositionsRatio().doubleValue());
        Assert.assertTrue(first.getWinningPositionsRatio().doubleValue() >= 0.5);
    }

    //    private TradingMetrics testStrategy(StrategyParameters parameters, StrategyProvider strategyProvider) {
    private TradingMetrics testStrategy(String symbol, StrategyProvider strategyProvider) {

        List<TickerQuote> quotes = TestDataProvider.getQuotesFromFile(symbol + ".json");
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy);

        log.info(strategy.getName());
        TradingMetrics metrics = TradingRecordUtils.buildTradingMetrics(symbol, series, tradingRecord);
        ConsoleUtils.printTradingRecords(symbol, tradingRecord);
        ConsoleUtils.printTradingMetrics(metrics);
        return metrics;
    }

}
