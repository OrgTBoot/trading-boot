package com.mg.trading.boot.tbd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema1.DEMAStrategyDefinition;
import com.mg.trading.boot.domain.strategy.dema2.DEMAStrategyDefinitionV2;
import com.mg.trading.boot.domain.strategy.dema3.DEMAStrategyDefinitionV3;
import com.mg.trading.boot.domain.strategy.ema.EMAStrategyDefinition;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.reports.TradingStatement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.mg.trading.boot.tbd.TestDataProvider.getQuotesFromFile;


@Log4j2
public class StrategyTest {

    @Before
    public void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
    }

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
        List<File> quoteFiles = TestDataProvider.getQuoteFiles();

        List<TradingStatement> statementsEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement ema = testStrategy(s, new EMAStrategyDefinition(s.getName()));
            statementsEMA.add(ema);
        });

        List<TradingStatement> statementsDEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement dema = testStrategy(s, new DEMAStrategyDefinition(s.getName()));
            statementsDEMA.add(dema);
        });

        List<TradingStatement> statementsDEMAv2 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV2 = testStrategy(s, new DEMAStrategyDefinitionV2(s.getName()));
            statementsDEMAv2.add(demaV2);
        });

        List<TradingStatement> statementsDEMAv3 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV3 = testStrategy(s, new DEMAStrategyDefinitionV3(s.getName()));
            statementsDEMAv3.add(demaV3);
        });

        AsciiTable table = new AsciiTable();
        reportToTable("EMA", statementsEMA, table);
        reportToTable("DEMA", statementsDEMA, table);
        reportToTable("DEMAv2", statementsDEMAv2, table);
        reportToTable("DEMAv3", statementsDEMAv3, table);
        print(table);
    }

    private static TradingStatement testStrategy(File file, StrategyDefinition def) {
        def.updateSeries(getQuotesFromFile(file));

        BarSeriesManager seriesManager = new BarSeriesManager(def.getSeries());
        TradingRecord tradingRecord = seriesManager.run(def.getStrategy());

        log.info(def.getClass().getSimpleName());
        ReportGenerator.printTradingRecords(tradingRecord, file.getName());
        ReportGenerator.printTradingSummary(tradingRecord, file.getName());

        return ReportGenerator.buildTradingStatement(tradingRecord);
    }

    private static void reportToTable(String name, List<TradingStatement> statements, AsciiTable table) {
        Function<List<TradingStatement>, Double> percent = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).sum();
        Function<List<TradingStatement>, Double> positions = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();
        Function<List<TradingStatement>, Double> winning = (s) -> s.stream().mapToDouble(ReportGenerator::winPositionsCount).sum();
        Function<List<TradingStatement>, Double> total = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();

        if (table.getColNumber() == 0) {
            table.addRule();
            table.addRow("STRATEGY", "DATA SETS", "GAIN %", "POSITIONS", "WINS RATIO").setTextAlignment(TextAlignment.CENTER);
            table.addRule();
        }
        table.addRow(name, statements.size(), percent.apply(statements) + "%", positions.apply(statements), winning.apply(statements) / total.apply(statements));
        table.addRule();
        table.getRenderer().setCWC(new CWC_LongestLine());
    }

    private void print(AsciiTable table) {
        log.info("\n" + table.render());
    }

}
