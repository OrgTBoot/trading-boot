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
import com.mg.trading.boot.domain.strategy.ema.EMAStrategyDefinition;
import com.mg.trading.boot.tbd.TestDataProvider;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.reports.TradingStatement;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
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
        List<File> quoteFiles = TestDataProvider.getQuoteFiles("./src/test/resources/11_08_2022");
//        List<File> quoteFiles = TestDataProvider.getQuoteFiles();

        AsciiTable table = new AsciiTable();

        List<TradingStatement> statementsEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement ema = testStrategy(s, new EMAStrategyDefinition(s.getName()));
            statementsEMA.add(ema);
        });
        reportToTable("EMA", statementsEMA, table);


        List<TradingStatement> statementsDEMA = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement dema = testStrategy(s, new DEMAStrategyDefinition(s.getName()));
            statementsDEMA.add(dema);
        });
        reportToTable("DEMA", statementsDEMA, table);


        List<TradingStatement> statementsDEMAv2 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV2 = testStrategy(s, new DEMAStrategyDefinitionV2(s.getName()));
            statementsDEMAv2.add(demaV2);
        });
        reportToTable("DEMAv2", statementsDEMAv2, table);


        List<TradingStatement> statementsDEMAv3 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV3 = testStrategy(s, new DEMAStrategyDefinitionV3(s.getName()));
            statementsDEMAv3.add(demaV3);
        });
        reportToTable("DEMAv3", statementsDEMAv3, table);

        List<TradingStatement> statementsDEMAv4 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV4 = testStrategy(s, new DEMAStrategyDefinitionV4(s.getName()));
            statementsDEMAv4.add(demaV4);
        });
        reportToTable("DEMAv4", statementsDEMAv4, table);

        List<TradingStatement> statementsDEMAv5 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV5 = testStrategy(s, new DEMAStrategyDefinitionV5(s.getName()));
            statementsDEMAv5.add(demaV5);
        });
        reportToTable("DEMAv5", statementsDEMAv5, table);

        List<TradingStatement> statementsDEMAv6 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV6 = testStrategy(s, new DEMAStrategyDefinitionV6(s.getName()));
            statementsDEMAv6.add(demaV6);
        });
        reportToTable("DEMAv6", statementsDEMAv6, table);


        List<TradingStatement> statementsDEMAv7 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV7 = testStrategy(s, new DEMAStrategyDefinitionV7(s.getName()));
            statementsDEMAv7.add(demaV7);
        });
        reportToTable("DEMAv7", statementsDEMAv7, table);


        List<TradingStatement> statementsDEMAv8 = new ArrayList<>();
        quoteFiles.forEach(s -> {
            TradingStatement demaV8 = testStrategy(s, new DEMAStrategyDefinitionV8(s.getName()));
            statementsDEMAv8.add(demaV8);
        });
        reportToTable("DEMAv8", statementsDEMAv8, table);


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
        DecimalFormat df = new DecimalFormat("0.000");
        Function<List<TradingStatement>, Double> percent = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).sum();
        Function<List<TradingStatement>, Double> positions = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();
        Function<List<TradingStatement>, Double> winning = (s) -> s.stream().mapToDouble(ReportGenerator::winPositionsCount).sum();
        Function<List<TradingStatement>, Double> total = (s) -> s.stream().mapToDouble(ReportGenerator::totalPositionsCount).sum();

        //from all traded symbols - get min, max, avg. This is useful when comparing with other strategies to identify the extremes.
        Function<List<TradingStatement>, Double> maxGain = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).max().getAsDouble();
        Function<List<TradingStatement>, Double> minGain = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).min().getAsDouble();
        Function<List<TradingStatement>, Double> avgGain = (s) -> s.stream().mapToDouble(ReportGenerator::totalInPercent).average().getAsDouble();

        Double winningRatio = winning.apply(statements) / total.apply(statements);


        if (table.getColNumber() == 0) {
            table.addRule();
            table.addRow("STRATEGY", "DATA SETS", "POSITIONS", "GAIN %", "MAX GAIN %", "MIN GAIN %", "AVG GAIN %", "WINS RATIO").setTextAlignment(TextAlignment.CENTER);
            table.addRule();
        }
        table.addRow(name,
                statements.size(),
                positions.apply(statements),
                df.format(percent.apply(statements)) + "%",
                df.format(maxGain.apply(statements)) + "%",
                df.format(minGain.apply(statements)) + "%",
                df.format(avgGain.apply(statements)) + "%",
                df.format(winningRatio));
        table.addRule();
        table.getRenderer().setCWC(new CWC_LongestLine());
    }

    private void print(AsciiTable table) {
        log.info("\n" + table.render());
    }

}
