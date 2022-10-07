package com.mg.trading.boot.utils;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.PerformanceReport;
import org.ta4j.core.reports.PositionStatsReport;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static com.mg.trading.boot.utils.NumberUtils.defaultToZero;
import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class ConsoleUtils {

    public static TradingStatement getTradingStatement(Strategy strategy, TradingRecord tradingRecord, BarSeries series) {
        return new TradingStatementGenerator().generate(strategy, tradingRecord, series);
    }

    public static void printTradingStatement(String symbol,
                                             Strategy strategy,
                                             TradingRecord tradingRecord,
                                             BarSeries series) {
        TradingStatement statement = getTradingStatement(strategy, tradingRecord, series);
        printTradingStatement(symbol, statement);
    }


    public static void printTradingStatement(String symbol, TradingStatement statement) {
        log.info("TRADING STATEMENT FOR STRATEGY: {}", statement.getStrategy().getName());
        PerformanceReport perf = statement.getPerformanceReport();
        PositionStatsReport stats = statement.getPositionStatsReport();

        final Num lossCount = stats.getLossCount();
        final Num winnCount = stats.getProfitCount().plus(stats.getBreakEvenCount());
        final Num totalCount = winnCount.plus(lossCount);
        final BigDecimal totalProfitLoss = toRndBigDecimal(perf.getTotalProfitLoss());
        final BigDecimal totalProfitLossPercentage = toRndBigDecimal(perf.getTotalProfitLossPercentage());

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "METRIC", "VALUE").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(symbol, "Total return", totalProfitLossPercentage + "% | " + totalProfitLoss + "$");
        table.addRule();
        table.addRow(symbol, "Winning positions ratio", getWinningRatio(statement));
        table.addRule();
        table.addRow(symbol, "Total positions | ↑ wins | ↓ losses ", totalCount + " | ↑" + winnCount + " | ↓" + lossCount);
        table.addRule();

        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }

    public static void printTradingRecords(String symbol, TradingRecord tradingRecord) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "ENTER IDX", "EXIT IDX", "SHARES", "ENTER PRICE", "EXIT PRICE", "PROFIT $", "PROFIT %").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        tradingRecord.getPositions().forEach(it -> {
            AT_Row row = table.addRow(
                    symbol,
                    it.getEntry().getIndex(),
                    it.getExit().getIndex(),
                    toRndBigDecimal(it.getEntry().getAmount()),
                    toRndBigDecimal(it.getEntry().getNetPrice()),
                    toRndBigDecimal(it.getExit().getNetPrice()),
                    toRndBigDecimal(it.getProfit()),
                    getPositionProfitInPercent(it) + "%");


            //align numbers to the left
            IntStream.of(3, 4, 5, 6, 7).forEach(colIdx -> row.getCells().get(colIdx).getContext().setTextAlignment(TextAlignment.RIGHT));
            table.addRule();
        });

        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }

    public static BigDecimal getWinningRatio(TradingStatement statement) {
        final Num profitCount = statement.getPositionStatsReport().getProfitCount();
        final Num evenCount = statement.getPositionStatsReport().getBreakEvenCount();
        final Num lossCount = statement.getPositionStatsReport().getLossCount();

        final Num totalCount = profitCount.plus(lossCount).plus(evenCount);
        if (totalCount == null) {
            return BigDecimal.ZERO;
        }
        final Num ratio = profitCount.dividedBy(totalCount);
        return toRndBigDecimal(ratio);
    }

    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toRndBigDecimal(percent);
    }
}
