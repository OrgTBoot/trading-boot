package com.mg.trading.boot.strategy.reporting;

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
import java.time.ZonedDateTime;
import java.util.stream.IntStream;

import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class TradingReportGenerator {
    private final String symbol;
    private final Strategy strategy;
    private final BarSeries series;

    public TradingReportGenerator(String symbol, Strategy strategy, BarSeries series) {
        this.symbol = symbol;
        this.strategy = strategy;
        this.series = series;
    }

    public TradingStatement getTradingStatement(TradingRecord tradingRecord) {
        return new TradingStatementGenerator().generate(strategy, tradingRecord, series);
    }

    public void printTradingRecords(TradingRecord tradingRecord) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "RANGE", "ENTER", "EXIT", "SHARES +", "SHARES -", "ENTER PRICE", "EXIT PRICE",
                "PROFIT $", "PROFIT %").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        tradingRecord.getPositions().forEach(it -> {
            AT_Row row = table.addRow(symbol,
                    getRange(it),
                    getActionTime(it.getEntry().getIndex()),
                    getActionTime(it.getExit().getIndex()),
                    toRndBigDecimal(it.getEntry().getAmount()),
                    toRndBigDecimal(it.getExit().getAmount()),
                    toRndBigDecimal(it.getEntry().getNetPrice()) + "$",
                    toRndBigDecimal(it.getExit().getNetPrice()) + "$",
                    toRndBigDecimal(it.getProfit()) + "$",
                    getPositionProfitInPercent(it) + "%");


            //align numbers to the left
            IntStream.of(2, 3, 4, 5, 6, 7).forEach(colIdx -> row.getCells().get(colIdx).getContext().setTextAlignment(TextAlignment.RIGHT));
            table.addRule();
        });

        printTable(table);
    }

    private String getRange(Position it) {
        return it.getEntry().getIndex() + "-" + it.getExit().getIndex();
    }

    private String getActionTime(int idx) {
        final Bar bar = series.getBar(idx);
        final ZonedDateTime endTime = bar.getEndTime();

        return String.format("%s:%s", endTime.getHour(), endTime.getMinute());
    }

    public void printTradingSummary(TradingRecord tradingRecord) {
        TradingStatement statement = getTradingStatement(tradingRecord);

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "METRIC SUMMARY", "VALUE").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(symbol, "Total return", totalInPercent(statement) + "% | " + totalInDollars(statement) + "$");
        table.addRule();
        table.addRow(symbol, "Winning positions ratio", winningRatio(statement));
        table.addRule();
        table.addRow(symbol, "Total positions | ↑ wins | ↓ losses | ~even ", totalCount(statement) + " | ↑" + winsCount(statement) + " | ↓" + lossesCount(statement) + " | ~" + breakEvenCount(statement));
        table.addRule();

        printTable(table);
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

    //--------------------------------------------------------
    //-------------Private Methods----------------------------
    //--------------------------------------------------------

    private void printTable(AsciiTable table) {
        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\nSTRATEGY: " + strategy.getName() + "\n" + table.render());
    }

    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toRndBigDecimal(percent);
    }
}
