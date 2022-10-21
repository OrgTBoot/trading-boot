package com.mg.trading.boot;

import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.IStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema.XDEMAStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.XDEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.strategy.ema.XEMAStrategyDefinition;
import com.mg.trading.boot.utils.BarSeriesUtils;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.reports.TradingStatement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


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

//    /**
//     * If allowed to be traded without a total loss tolerance threshold there is a risk we can lose too much.
//     * To avoid such scenario we apply a total loss tolerance of X%, when the threashold is reached
//     * we prevent us from entering in to new position. It's a bad day/stock - STOP!!!
//     * Ex:
//     * ┌──────┬────────────────────────────────────────────┬──────────────────────────┐
//     * │SYMBOL│                   METRIC                   │          VALUE           │
//     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
//     * │SYTA  │Total return                                │-23.09% | -0.03139$       │
//     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
//     * │SYTA  │Winning positions ratio                     │0.09091                   │
//     * ├──────┼────────────────────────────────────────────┼──────────────────────────┤
//     * │SYTA  │Total positions | ↑ wins | ↓ losses | ~even │11.0 | ↑1.0 | ↓10.0 | ~0.0│
//     * └──────┴────────────────────────────────────────────┴──────────────────────────┘
//     */
//    @Test
//    public void testStrategyTotalLossToleranceDEMA() {
//        String symbol = "SYTA_loss_tolerance";
//        TradingStatement emaStatement = testStrategy(symbol, new DEMAStrategyProvider(symbol, BigDecimal.ONE));
//        Assert.assertEquals(-4.008, totalInPercent(emaStatement), 0);
//    }

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
        symbols.add("BHG_10_11_2022");
        symbols.add("AMD_10_13_2022");
        symbols.add("NET_10_17_2022");
        symbols.add("REI_10_17_2022");
        symbols.add("10_19_2022_AMPX");
        symbols.add("10_19_2022_ESTE");
        symbols.add("10_19_2022_MNTV");
        symbols.add("10_19_2022_MVST");
        symbols.add("10_19_2022_ZYME");
        symbols.add("10_20_2020_WKHS");
        symbols.add("10_20_2022_TSLA");
        symbols.add("10_20_20200_AMD");


        List<TradingStatement> statementsEMA = new ArrayList<>();
        symbols.forEach(s -> {
            TradingStatement ema = testXStrategy(s, new XEMAStrategyDefinition(s));
            statementsEMA.add(ema);
        });

        List<TradingStatement> statementsDEMA = new ArrayList<>();
        symbols.forEach(s -> {
            TradingStatement dema = testXStrategy(s, new XDEMAStrategyDefinition(s));
            statementsDEMA.add(dema);
        });

        List<TradingStatement> statementsDEMAv2 = new ArrayList<>();
        symbols.forEach(s -> {
            TradingStatement demaV2 = testXStrategy(s, new XDEMAStrategyDefinitionV2(s));
            statementsDEMAv2.add(demaV2);
        });

        printTradingSummaries("EMA", statementsEMA, "DEMA", statementsDEMA, "DEMAv2", statementsDEMAv2);
    }

    private static TradingStatement testXStrategy(String symbol, IStrategyDefinition def) {

        List<TickerQuote> quotes = TestDataProvider.getQuotesFromFile(symbol + ".json");
//        BarSeries series = new BaseBarSeries();
//        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        def.updateSeries(quotes);
        BarSeriesManager seriesManager = new BarSeriesManager(def.getSeries());

        TradingRecord tradingRecord = seriesManager.run(def.getStrategy());

        log.info(def.getClass().getSimpleName());
        ReportGenerator.printTradingRecords(tradingRecord, symbol);
        ReportGenerator.printTradingSummary(tradingRecord, symbol);

        return ReportGenerator.buildTradingStatement(tradingRecord);
    }

    private static void printTradingSummaries(String name1, List<TradingStatement> list1, String name2, List<TradingStatement> list2, String name3, List<TradingStatement> list3) {
        Function<List<TradingStatement>, Double> percent = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).sum();
        Function<List<TradingStatement>, Double> positions = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();
        Function<List<TradingStatement>, Double> winning = (s) -> s.stream().mapToDouble(ReportGenerator::winPositionsCount).sum();
        Function<List<TradingStatement>, Double> total = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();


        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("STRATEGY", "DATA SETS", "GAIN %", "POSITIONS", "WINS RATIO").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(name1, list1.size(), percent.apply(list1) + "%", positions.apply(list1), winning.apply(list1) / total.apply(list1));
        table.addRule();
        table.addRow(name2, list2.size(), percent.apply(list2) + "%", positions.apply(list2), winning.apply(list2) / total.apply(list2));
        table.addRule();
        table.addRow(name3, list3.size(), percent.apply(list3) + "%", positions.apply(list3), winning.apply(list3) / total.apply(list3));
        table.addRule();
        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }


//    private static void assertFirstTradingStatementBeatsSecond(TradingStatement first, TradingStatement second) {
//        final double winningRatio1 = winningRatio(first);
//        final double totalProfitLoss1 = totalInDollars(first);
//        final double totalProfitLossPercentage1 = totalInPercent(first);
//
//        final double winningRatio2 = winningRatio(second);
//        final double totalProfitLoss2 = totalInDollars(second);
//        final double totalProfitLossPercentage2 = totalInPercent(second);
//
//        Assert.assertTrue("Profit of the first (" + totalProfitLossPercentage1 + ") < second (" + totalProfitLossPercentage2 + ")", totalProfitLossPercentage1 >= totalProfitLossPercentage2);
//        Assert.assertTrue(totalProfitLoss1 >= totalProfitLoss2);
//        Assert.assertTrue(winningRatio1 >= 0.5 || winningRatio1 == 0);
//        Assert.assertTrue("Winning ration of the first (" + winningRatio1 + ") < second (" + winningRatio2 + ")", winningRatio1 >= winningRatio2);
//    }

}
