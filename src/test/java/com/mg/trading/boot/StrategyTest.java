package com.mg.trading.boot;

import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.goldencross.DEMAStrategyProvider;
import com.mg.trading.boot.strategy.goldencross.EMAStrategyProvider;
import com.mg.trading.boot.strategy.goldencross.StrategyProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import com.mg.trading.boot.utils.ConsoleUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.reports.TradingStatement;

import java.time.Duration;
import java.util.List;

import static com.mg.trading.boot.utils.ConsoleUtils.getWinningRatio;
import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class StrategyTest {

    /**
     * Running two data sets through the same strategy.
     * <p>
     * WB data seems to be of a better quality.
     * This confirms that for day trading, it is better to use WB as a source for quotes.
     */
    @Test
    public void testIMVT_same_period_different_providers() {
        String symbolWB = "IMVT_WB_1DAY_1MIN";
        TradingStatement weboll = testStrategy(symbolWB, new DEMAStrategyProvider(symbolWB));

        String symbolYO = "IMVT_YO_1DAY_1MIN";
        TradingStatement yahoo = testStrategy(symbolYO, new DEMAStrategyProvider(symbolYO));

        assertFirstTradingStatementBeatsSecond(weboll, yahoo);
    }

    @Test
    public void testINVT_gainScenario() {
        String symbol = "IMVT_1DAY_1MIN";
        TradingStatement emaStatement = testStrategy(symbol, new EMAStrategyProvider(symbol));
        Assert.assertEquals(0.6667, winningRatio(emaStatement), 0);
        Assert.assertEquals(2, winsCount(emaStatement), 0);
        Assert.assertEquals(1, lossesCount(emaStatement), 0);
        Assert.assertEquals(4.568, totalInPercent(emaStatement), 0);
        Assert.assertEquals(0.4200, totalInDollars(emaStatement), 0);

        TradingStatement demaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol));
        Assert.assertEquals(0.75, winningRatio(demaStatement), 0);
        Assert.assertEquals(3, winsCount(demaStatement), 0);
        Assert.assertEquals(1, lossesCount(demaStatement), 0);
        Assert.assertEquals(7.551, totalInPercent(demaStatement), 0);
        Assert.assertEquals(0.7000, totalInDollars(demaStatement), 0);

        assertFirstTradingStatementBeatsSecond(demaStatement, emaStatement);
    }

    @Test
    public void testOPEN_lossScenario() {
        String symbol = "OPEN";
        TradingStatement emaStatement = testStrategy(symbol, new EMAStrategyProvider(symbol));
        Assert.assertEquals(0.5, winningRatio(emaStatement), 0);
        Assert.assertEquals(1, winsCount(emaStatement), 0);
        Assert.assertEquals(1, lossesCount(emaStatement), 0);
        Assert.assertEquals(-1.486, totalInPercent(emaStatement), 0);
        Assert.assertEquals(-0.05099, totalInDollars(emaStatement), 0);

        TradingStatement demaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol));
        Assert.assertEquals(0.5, winningRatio(demaStatement), 0);
        Assert.assertEquals(1, winsCount(demaStatement), 0);
        Assert.assertEquals(1, lossesCount(demaStatement), 0);
        Assert.assertEquals(-0.9259, totalInPercent(demaStatement), 0);
        Assert.assertEquals(-0.03200, totalInDollars(demaStatement), 0);

        assertFirstTradingStatementBeatsSecond(demaStatement, emaStatement);
    }


    @Test
    public void testAMPY_noOpenPosition() {
        String symbol = "AMPY_1DAY_1MIN";
        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));

        assertFirstTradingStatementBeatsSecond(dema, ema);
    }

    @Test
    public void testPNT_gainScenario() {
        String symbol = "PNT_1DAY_1MIN";
        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));

        assertFirstTradingStatementBeatsSecond(dema, ema);
    }

    @Test
    public void testEGY_gainScenario() {
        String symbol = "EGY_1DAY_1MIN";
        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));

        assertFirstTradingStatementBeatsSecond(dema, ema);
    }


    private static void assertFirstTradingStatementBeatsSecond(TradingStatement first, TradingStatement second) {
        final double lossCount1 = lossesCount(first);
        final double winCount1 = winsCount(first);
        final double winningRatio1 = winningRatio(first);
        final double totalProfitLoss1 = totalInDollars(first);
        final double totalProfitLossPercentage1 = totalInPercent(first);

        final double lossCount2 = lossesCount(second);
        final double winCount2 = winsCount(second);
        final double winningRatio2 = winningRatio(second);
        final double totalProfitLoss2 = totalInDollars(second);
        final double totalProfitLossPercentage2 = totalInPercent(second);

        Assert.assertTrue("Profit of the first (" + totalProfitLossPercentage1 + ") < second (" + totalProfitLossPercentage2 + ")", totalProfitLossPercentage1 >= totalProfitLossPercentage2);
        Assert.assertTrue(totalProfitLoss1 >= totalProfitLoss2);
        Assert.assertTrue(winningRatio1 >= 0.5 || winningRatio1 == 0);
        Assert.assertTrue("Winning ration of the first (" + winningRatio1 + ") < second (" + winningRatio2 + ")", winningRatio1 >= winningRatio2);
    }

    private static Double winningRatio(TradingStatement statement) {
        return getWinningRatio(statement).doubleValue();
    }

    private static Double winsCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getProfitCount().doubleValue();
    }

    private static Double lossesCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getLossCount().doubleValue();
    }

    private static Double totalInPercent(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLossPercentage()).doubleValue();
    }

    private static Double totalInDollars(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLoss()).doubleValue();
    }

    private static TradingStatement testStrategy(String symbol, StrategyProvider strategyProvider) {

        List<TickerQuote> quotes = TestDataProvider.getQuotesFromFile(symbol + ".json");
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy);

        TradingStatement tradingStatement = ConsoleUtils.getTradingStatement(strategy, tradingRecord, series);
        ConsoleUtils.printTradingRecords(symbol, tradingRecord);
        ConsoleUtils.printTradingStatement(symbol, tradingStatement);
        return tradingStatement;
    }

}
