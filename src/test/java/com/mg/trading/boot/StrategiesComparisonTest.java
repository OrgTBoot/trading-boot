package com.mg.trading.boot;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mg.trading.boot.domain.reporting.ReportGenerator;
import com.mg.trading.boot.domain.strategy.StrategyDefinition;
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
import com.mg.trading.boot.domain.strategy.supertrend.SuperTrendStrategyV1;
import com.mg.trading.boot.tbd.TestDataProvider;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.reports.TradingStatement;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.mg.trading.boot.tbd.TestDataProvider.getQuotesFromFile;


@Log4j2
public class StrategiesComparisonTest {

    @Before
    public void setUp() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
    }

    @Test
    public void testStrategiesGain() {
        List<File> quoteFiles = TestDataProvider.getQuoteFiles();
//        List<File> quoteFiles = TestDataProvider.getQuoteFiles("./src/test/resources/11_15_2022_red");
//        List<File> quoteFiles = TestDataProvider.getQuoteFiles("./src/test/resources/11_18_2022_red");
//        List<File> quoteFiles = TestDataProvider.getQuoteFiles("./src/test/resources/11_17_2022");
//        List<File> quoteFiles = TestDataProvider.getQuoteFiles("./src/test/resources/tmp");

        AsciiTable table = new AsciiTable();

        List<TradingStatementWrapper> statementsEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper ema = testStrategy(s, new EMAStrategyDefinition(s.getName()));
            statementsEMA.add(ema);
        });
        reportToTable("EMA", statementsEMA, table);


        List<TradingStatementWrapper> statementsDEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper dema = testStrategy(s, new DEMAStrategyDefinition(s.getName()));
            statementsDEMA.add(dema);
        });
        reportToTable("DEMA", statementsDEMA, table);


        List<TradingStatementWrapper> statementsDEMAv2 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV2 = testStrategy(s, new DEMAStrategyDefinitionV2(s.getName()));
            statementsDEMAv2.add(demaV2);
        });
        reportToTable("DEMAv2", statementsDEMAv2, table);


        List<TradingStatementWrapper> statementsDEMAv3 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV3 = testStrategy(s, new DEMAStrategyDefinitionV3(s.getName()));
            statementsDEMAv3.add(demaV3);
        });
        reportToTable("DEMAv3", statementsDEMAv3, table);

        List<TradingStatementWrapper> statementsDEMAv4 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV4 = testStrategy(s, new DEMAStrategyDefinitionV4(s.getName()));
            statementsDEMAv4.add(demaV4);
        });
        reportToTable("DEMAv4", statementsDEMAv4, table);

        List<TradingStatementWrapper> statementsDEMAv5 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV5 = testStrategy(s, new DEMAStrategyDefinitionV5(s.getName()));
            statementsDEMAv5.add(demaV5);
        });
        reportToTable("DEMAv5", statementsDEMAv5, table);

        List<TradingStatementWrapper> statementsDEMAv6 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV6 = testStrategy(s, new DEMAStrategyDefinitionV6(s.getName()));
            statementsDEMAv6.add(demaV6);
        });
        reportToTable("DEMAv6", statementsDEMAv6, table);


        List<TradingStatementWrapper> statementsDEMAv7 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV7 = testStrategy(s, new DEMAStrategyDefinitionV7(s.getName()));
            statementsDEMAv7.add(demaV7);
        });
        reportToTable("DEMAv7", statementsDEMAv7, table);


        List<TradingStatementWrapper> statementsDEMAv8 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV8 = testStrategy(s, new DEMAStrategyDefinitionV8(s.getName()));
            statementsDEMAv8.add(demaV8);
        });
        reportToTable("DEMAv8", statementsDEMAv8, table);

        List<TradingStatementWrapper> statementsDEMAv9 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper demaV9 = testStrategy(s, new DEMAStrategyDefinitionV9(s.getName()));
            statementsDEMAv9.add(demaV9);
        });
        reportToTable("DEMAv9", statementsDEMAv9, table);


        List<TradingStatementWrapper> statementsST1 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatementWrapper st1 = testStrategy(s, new SuperTrendStrategyV1(s.getName()));
            statementsST1.add(st1);
        });
        reportToTable("ST1", statementsST1, table);


        print(table);
    }

    private static TradingStatementWrapper testStrategy(File file, StrategyDefinition def) {
        def.updateSeries(getQuotesFromFile(file));

        BarSeriesManager seriesManager = new BarSeriesManager(def.getSeries());
        TradingRecord record = seriesManager.run(def.getStrategy());

        log.info(def.getClass().getSimpleName());
        ReportGenerator.printTradingRecords(record, file.getName());
        ReportGenerator.printTradingSummary(record, file.getName());

        TradingStatement statement = ReportGenerator.buildTradingStatement(record);

        return new TradingStatementWrapper(statement, record);
    }

    @Data
    @AllArgsConstructor
    static class TradingStatementWrapper {
        private TradingStatement statement;
        private TradingRecord record;
    }

    private static void reportToTable(String name, List<TradingStatementWrapper> statements, AsciiTable table) {
        DecimalFormat df = new DecimalFormat("0.000");
        Function<List<TradingStatementWrapper>, Double> percent = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalInPercent(it.statement)).sum();
        Function<List<TradingStatementWrapper>, Double> dollarsEntry = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalEntryPositionsAmountInDollars(it.record)).sum();
        Function<List<TradingStatementWrapper>, Double> dollarsExit = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalExitPositionsAmountInDollars(it.record)).sum();
        Function<List<TradingStatementWrapper>, Double> dollarsGained = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalInDollars(it.statement)).sum();
        Function<List<TradingStatementWrapper>, Double> positions = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalPositionsCount(it.statement)).sum();
        Function<List<TradingStatementWrapper>, Double> winning = (s) -> s.stream().mapToDouble(it -> ReportGenerator.winPositionsCount(it.statement)).sum();
        Function<List<TradingStatementWrapper>, Double> total = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalPositionsCount(it.statement)).sum();

        //from all traded symbols - get min, max, avg. This is useful when comparing with other strategies to identify the extremes.
        Function<List<TradingStatementWrapper>, Double> maxGain = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalInPercent(it.statement)).max().getAsDouble();
        Function<List<TradingStatementWrapper>, Double> minGain = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalInPercent(it.statement)).min().getAsDouble();
        Function<List<TradingStatementWrapper>, Double> avgGain = (s) -> s.stream().mapToDouble(it -> ReportGenerator.totalInPercent(it.statement)).average().getAsDouble();

        Double winningRatio = winning.apply(statements) / total.apply(statements);
        Double percent4 = dollarsGained.apply(statements) * 100 / dollarsEntry.apply(statements);
        Double percent5 = percent.apply(statements) / positions.apply(statements);

        if (table.getColNumber() == 0) {
            table.addRule();
            table.addRow("STRATEGY", "DATA SETS", "POSITIONS", "MIN P.GAIN %", "MAX P.GAIN %", "AVG GAIN %", "GAIN $", "GAIN %", "WINS RATIO").setTextAlignment(TextAlignment.CENTER);
            table.addRule();
        }
        table.addRow(name,
                statements.size(),
                positions.apply(statements),
                df.format(minGain.apply(statements)) + "%",
                df.format(maxGain.apply(statements)) + "%",
                df.format(avgGain.apply(statements)) + "%",
                df.format(dollarsGained.apply(statements)) + "$",
                df.format(percent.apply(statements)) + "%",
//                df.format(percent4) + "%",
                df.format(winningRatio));
        table.addRule();
        table.getRenderer().setCWC(new CWC_LongestLine());
    }

    private void print(AsciiTable table) {
        log.info("\n" + table.render());
    }

}
