package com.mg.trading.boot.strategy.reporting;

import com.mg.trading.boot.models.Order;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.*;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class ReportGenerator {
    private final String symbol;
    private final Strategy strategy;
    private final BarSeries series;

    public ReportGenerator(String symbol, Strategy strategy, BarSeries series) {
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
        table.addRow(symbol, "Total positions | ↑ wins | ↓ losses | ~even ", totalPositionsCount(statement) + " | ↑" + winPositionsCount(statement) + " | ↓" + lossPositionsCount(statement) + " | ~" + breakEvenPositionsCount(statement));
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

    public static Double winPositionsCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getProfitCount().doubleValue();
    }

    public static Double totalPositionsCount(TradingStatement statement) {
        return winPositionsCount(statement) + lossPositionsCount(statement) + breakEvenPositionsCount(statement);
    }

    public static Double lossPositionsCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getLossCount().doubleValue();
    }

    public static Double breakEvenPositionsCount(TradingStatement statement) {
        return statement.getPositionStatsReport().getBreakEvenCount().doubleValue();
    }

    public static Double totalInPercent(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLossPercentage()).doubleValue();
    }

    public static Double totalInDollars(TradingStatement statement) {
        return toRndBigDecimal(statement.getPerformanceReport().getTotalProfitLoss()).doubleValue();
    }

    /**
     * Aggregates orders that have more than one same consecutive action.
     * For example a list of orders [BUY, BUY, SELL, BUY] will result in an aggregated list [BUY, SELL, BUY]
     *
     * @param orders - orders for aggregation
     * @return - aggregated list of orders
     */
    public static List<Order> aggregateOrders(List<Order> orders) {
        List<Order> aggRecords = new ArrayList<>();

        for (Order current : orders) {
            Order prev = CollectionUtils.isEmpty(aggRecords) ? null : aggRecords.get(aggRecords.size() - 1);

            if (prev == null) {
                aggRecords.add(current);
                continue;
            }

            boolean sameAction = prev.getAction().equals(current.getAction());
            if (!sameAction) {
                aggRecords.add(current);
            } else {
                Order aggOrder = Order.builder()
                        .id(prev.getId() + "_agg_" + current.getId())
                        .action(prev.getAction())
                        .status(prev.getStatus())
                        .ticker(prev.getTicker())
                        .orderType(prev.getOrderType())
                        .lmtPrice(current.getLmtPrice())
                        .placedTime(current.getPlacedTime())
                        .filledTime(current.getFilledTime())
                        .timeInForce(current.getTimeInForce())
                        .totalQuantity(prev.getTotalQuantity().add(current.getTotalQuantity()))
                        .filledQuantity(prev.getFilledQuantity().add(current.getFilledQuantity()))
                        .avgFilledPrice(prev.getAvgFilledPrice().add(current.getAvgFilledPrice()).divide(BigDecimal.valueOf(2), RoundingMode.CEILING))
                        .build();

                int lastIdx = aggRecords.size() - 1;
                aggRecords.remove(lastIdx);
                aggRecords.add(aggOrder);
            }
        }
        return aggRecords;
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

    private String getRange(Position it) {
        return it.getEntry().getIndex() + "-" + it.getExit().getIndex();
    }

    private String getActionTime(int idx) {
        final Bar bar = series.getBar(idx);
        final ZonedDateTime endTime = bar.getEndTime();

        return String.format("%s:%s", endTime.getHour(), endTime.getMinute());
    }
}
