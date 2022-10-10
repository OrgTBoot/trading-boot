package com.mg.trading.boot.utils;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

import java.math.BigDecimal;
import java.util.stream.IntStream;

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
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "METRIC", "VALUE").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(symbol, "Total return", totalInPercent(statement) + "% | " + totalInDollars(statement) + "$");
        table.addRule();
        table.addRow(symbol, "Winning positions ratio", winningRatio(statement));
        table.addRule();
        table.addRow(symbol, "Total positions | ↑ wins | ↓ losses | ~even ",
                totalCount(statement) + " | ↑" + winsCount(statement) + " | ↓" + lossesCount(statement) + " | ~" + breakEvenCount(statement));
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

    public static Double winningRatio(TradingStatement statement) {
        final Num profitCount = statement.getPositionStatsReport().getProfitCount();
        final Num evenCount = statement.getPositionStatsReport().getBreakEvenCount();
        final Num lossCount = statement.getPositionStatsReport().getLossCount();

        final Num totalCount = profitCount.plus(lossCount).plus(evenCount);
        if (totalCount == null) {
            return 0D;
        }
        final Num ratio = profitCount.dividedBy(totalCount);
        return toRndBigDecimal(ratio).doubleValue();
    }

    public static Double winsCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getProfitCount().doubleValue();
    }

    public static Double totalCount(TradingStatement statement) {
        return winsCount(statement) + lossesCount(statement);
    }

    public static Double lossesCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getLossCount().doubleValue();
    }

    public static Double breakEvenCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getBreakEvenCount().doubleValue();
    }

    public static Double totalInPercent(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLossPercentage()).doubleValue();
    }

    public static Double totalInDollars(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLoss()).doubleValue();
    }

    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toRndBigDecimal(percent);
    }

}
