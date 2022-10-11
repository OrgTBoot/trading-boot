package com.mg.trading.boot;

import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.strategy.core.StrategyProvider;
import com.mg.trading.boot.strategy.dema.DEMAStrategyProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.reports.TradingStatement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.mg.trading.boot.utils.ConsoleUtils.*;

@Log4j2
public class StrategyTest {

//    /**
//     * Running two data sets through the same strategy.
//     * <p>
//     * WB data seems to be of a better quality.
//     * This confirms that for day trading, it is better to use WB as a source for quotes.
//     */
//    @Test
//    public void testIMVT_same_period_different_providers() {
//        String symbolWB = "IMVT_WB_1DAY_1MIN";
//        TradingStatement weboll = testStrategy(symbolWB, new DEMAStrategyProvider(symbolWB));
//
//        String symbolYO = "IMVT_YO_1DAY_1MIN";
//        TradingStatement yahoo = testStrategy(symbolYO, new DEMAStrategyProvider(symbolYO));
//
//        assertFirstTradingStatementBeatsSecond(weboll, yahoo);
//    }
//
//    @Test
//    public void testINVT_gainScenario() {
//        String symbol = "IMVT_1DAY_1MIN";
//        TradingStatement emaStatement = testStrategy(symbol, new EMAStrategyProvider(symbol));
//        Assert.assertEquals(0.6667, winningRatio(emaStatement), 0);
//        Assert.assertEquals(2, winsCount(emaStatement), 0);
//        Assert.assertEquals(1, lossesCount(emaStatement), 0);
//        Assert.assertEquals(4.568, totalInPercent(emaStatement), 0);
//        Assert.assertEquals(0.4200, totalInDollars(emaStatement), 0);
//
//        TradingStatement demaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol));
//        Assert.assertEquals(0.5, winningRatio(demaStatement), 0);
//        Assert.assertEquals(4, winsCount(demaStatement), 0);
//        Assert.assertEquals(4, lossesCount(demaStatement), 0);
//        Assert.assertEquals(3.148, totalInPercent(demaStatement), 0);
//        Assert.assertEquals(0.29, totalInDollars(demaStatement), 0);
//
//        assertFirstTradingStatementBeatsSecond(demaStatement, emaStatement);
//    }
//
//    @Test
//    public void testOPEN_lossScenario() {
//        String symbol = "OPEN";
//        TradingStatement emaStatement = testStrategy(symbol, new EMAStrategyProvider(symbol));
//        Assert.assertEquals(0.5, winningRatio(emaStatement), 0);
//        Assert.assertEquals(1, winsCount(emaStatement), 0);
//        Assert.assertEquals(1, lossesCount(emaStatement), 0);
//        Assert.assertEquals(-1.486, totalInPercent(emaStatement), 0);
//        Assert.assertEquals(-0.05099, totalInDollars(emaStatement), 0);
//
//        TradingStatement demaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol));
//        Assert.assertEquals(0.5, winningRatio(demaStatement), 0);
//        Assert.assertEquals(2, winsCount(demaStatement), 0);
//        Assert.assertEquals(2, lossesCount(demaStatement), 0);
//        Assert.assertEquals(-0.7213, totalInPercent(demaStatement), 0);
//        Assert.assertEquals(-0.025, totalInDollars(demaStatement), 0);
//
//        assertFirstTradingStatementBeatsSecond(demaStatement, emaStatement);
//    }
//
//
//    @Test
//    public void testAMPY_noOpenPosition() {
//        String symbol = "AMPY_1DAY_1MIN";
//        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
//        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));
//
//        assertFirstTradingStatementBeatsSecond(dema, ema);
//    }
//
//    @Test
//    public void testPNT_gainScenario() {
//        String symbol = "PNT_1DAY_1MIN";
//        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
//        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));
//
//        assertFirstTradingStatementBeatsSecond(dema, ema);
//    }
//
//    /**
//     * For this case EMA beats DEMA :(
//     */
//    @Test
//    public void testEGY_gainScenario() {
//        String symbol = "EGY_1DAY_1MIN";
//        TradingStatement ema = testStrategy(symbol, new EMAStrategyProvider(symbol));
//        TradingStatement dema = testStrategy(symbol, new DEMAStrategyProvider(symbol));
//
//        assertFirstTradingStatementBeatsSecond(dema, ema);
//    }

    /**
     * If allowed to be traded without a total loss tolerance threshold there is a risk we can lose too much.
     * To avoid such scenario we apply a total loss tolerance of X%, when the threashold is reached
     * we prevent us from entering in to new position. It's a bad day/stock!!!
     * Ex:
     * ┌──────┬────────────────────────────────────────────┬──────────────────────────┐
     * │SYMBOL│                   METRIC                   │          VALUE           │
     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
     * │SYTA  │Total return                                │-23.09% | -0.03139$       │
     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
     * │SYTA  │Winning positions ratio                     │0.09091                   │
     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
     * │SYTA  │Total positions | ↑ wins | ↓ losses | ~even │11.0 | ↑1.0 | ↓10.0 | ~0.0│
     * └──────┴────────────────────────────────────────────┴──────────────────────────┘
     */
    @Test
    public void testStrategyTotalLossTolerance() {
        String symbol = "SYTA_loss_tolerance";
        TradingStatement emaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol));
        Assert.assertEquals(-4.008, totalInPercent(emaStatement), 0);
    }

    @Test
    public void testMultipleStocks() {
        List<String> symbols = new ArrayList<>();
        symbols.add("OPEN");
        symbols.add("PRVB_10_10_2022");
        symbols.add("IMVT_10_08_2022");
        symbols.add("IMVT_1DAY_1MIN");
        symbols.add("ETMB_1DAY_1MIN");
        symbols.add("TELL_1DAY_1MIN");
        symbols.add("AMPY_1DAY_1MIN");
        symbols.add("WTI_1DAY_IMIN_BERISH");
        symbols.add("SYTA_loss_tolerance");


        symbols.forEach(s -> testStrategy(s, new DEMAStrategyProvider(s)));
    }

    private static void assertFirstTradingStatementBeatsSecond(TradingStatement first, TradingStatement second) {
        final double winningRatio1 = winningRatio(first);
        final double totalProfitLoss1 = totalInDollars(first);
        final double totalProfitLossPercentage1 = totalInPercent(first);

        final double winningRatio2 = winningRatio(second);
        final double totalProfitLoss2 = totalInDollars(second);
        final double totalProfitLossPercentage2 = totalInPercent(second);

        Assert.assertTrue("Profit of the first (" + totalProfitLossPercentage1 + ") < second (" + totalProfitLossPercentage2 + ")", totalProfitLossPercentage1 >= totalProfitLossPercentage2);
        Assert.assertTrue(totalProfitLoss1 >= totalProfitLoss2);
        Assert.assertTrue(winningRatio1 >= 0.5 || winningRatio1 == 0);
        Assert.assertTrue("Winning ration of the first (" + winningRatio1 + ") < second (" + winningRatio2 + ")", winningRatio1 >= winningRatio2);
    }

    private static TradingStatement testStrategy(String symbol, StrategyProvider strategyProvider) {

        List<TickerQuote> quotes = TestDataProvider.getQuotesFromFile(symbol + ".json");
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        Strategy strategy = strategyProvider.buildStrategy(series).getStrategy();
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(strategy);

        TradingStatement tradingStatement = getTradingStatement(strategy, tradingRecord, series);
//        printTradingRecords(symbol, tradingRecord);
        printTradingStatement(symbol, tradingStatement);

        return tradingStatement;
    }

}
