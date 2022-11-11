package com.mg.trading.boot.domain.reporting;

import com.mg.trading.boot.domain.models.Order;
import com.mg.trading.boot.domain.models.OrderAction;
import com.mg.trading.boot.domain.models.TradingLog;
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
import org.ta4j.core.rules.BooleanRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.mg.trading.boot.utils.NumberUtils.toDecimalNum;
import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;

@Log4j2
public class ReportGenerator {

    public static void printTradingRecords(TradingLog tradingLog) {
        log.info("TRADING RECORDS FOR THE LAST {} DAY(S)", tradingLog.getDaysRange());
        TradingRecord tradingRecord = buildTradingRecord(tradingLog.getFilledOrders(), null);
        printTradingRecords(tradingRecord, tradingLog.getSymbol());
    }

    public static void printTradingRecords(TradingRecord tradingRecord, String symbol) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "ENTER", "EXIT", "SHARES +", "SHARES -", "ENTER PRICE", "EXIT PRICE", "PROFIT $", "PROFIT %").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        tradingRecord.getPositions().forEach(it -> {
            AT_Row row = table.addRow(symbol, it.getEntry().getIndex(), it.getExit().getIndex(), toRndBigDecimal(it.getEntry().getAmount()), toRndBigDecimal(it.getExit().getAmount()), toRndBigDecimal(it.getEntry().getNetPrice()) + "$", toRndBigDecimal(it.getExit().getNetPrice()) + "$", toRndBigDecimal(it.getProfit()) + "$", getPositionProfitInPercent(it) + "%");


            //align numbers to the left
            IntStream.of(3, 4, 5, 6, 7, 8).forEach(colIdx -> row.getCells().get(colIdx).getContext().setTextAlignment(TextAlignment.RIGHT));
            table.addRule();
        });
        printTable(table);
    }

    public static void printTradingSummary(TradingLog tradingLog) {
        log.info("Trading summary for the last {} day(s)", tradingLog.getDaysRange());
        TradingRecord tradingRecord = buildTradingRecord(tradingLog.getFilledOrders(), null);
        TradingStatement statement = buildTradingStatement(tradingRecord);
        printTradingSummary(statement, tradingLog.getSymbol());
    }

    public static void printTradingSummary(TradingRecord tradingRecord, String symbol) {
        TradingStatement statement = buildTradingStatement(tradingRecord);
        printTradingSummary(statement, symbol);
    }

    public static void printTradingSummary(TradingStatement statement, String symbol) {

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

    /**
     * Aggregates orders that have more than one same consecutive action.
     * For example a list of orders [BUY, BUY, SELL, BUY] will result in an aggregated list [BUY, SELL, BUY]
     *
     * @param orders - orders for aggregation
     * @return - aggregated list of orders
     */
    public static List<Order> aggregateOrders(List<Order> orders) {
        List<Order> aggRecords = new ArrayList<>();

        removeFirstIfSalle(orders);

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
                Order aggOrder = Order.builder().id(prev.getId() + "_agg_" + current.getId()).action(prev.getAction()).status(prev.getStatus()).ticker(prev.getTicker()).orderType(prev.getOrderType()).lmtPrice(current.getLmtPrice()).placedTime(current.getPlacedTime()).filledTime(current.getFilledTime()).timeInForce(current.getTimeInForce()).totalQuantity(prev.getTotalQuantity().add(current.getTotalQuantity())).filledQuantity(prev.getFilledQuantity().add(current.getFilledQuantity())).avgFilledPrice(prev.getAvgFilledPrice().add(current.getAvgFilledPrice()).divide(BigDecimal.valueOf(2), RoundingMode.CEILING)).build();

                int lastIdx = aggRecords.size() - 1;
                aggRecords.remove(lastIdx);
                aggRecords.add(aggOrder);
            }
        }
        return aggRecords;
    }

    public static TradingStatement buildTradingStatement(TradingRecord tradingRecord) {
        BooleanRule dummyRule = new BooleanRule(false);
        BaseBarSeries dummySeries = new BaseBarSeries();
        BaseStrategy dummyStrategy = new BaseStrategy("UNKNOWN (REPORTING)", dummyRule, dummyRule);
        return new TradingStatementGenerator().generate(dummyStrategy, tradingRecord, dummySeries);
    }

    public static TradingRecord buildTradingRecord(List<Order> orders, com.mg.trading.boot.domain.models.Position openPosition) {
        List<Order> aggOrders = aggregateOrders(orders); // covers BUY, BUY, SEL scenarios

        if (orders.size() != aggOrders.size()) {
            log.debug("There was a need to aggregate some of the orders, was {}, became {}", orders.size(), aggOrders.size());
        }

        TradingRecord tradingRecord = new BaseTradingRecord();

        if (CollectionUtils.isEmpty(orders) && openPosition != null) {
            Num price = toDecimalNum(openPosition.getCostPrice());
            Num qty = toDecimalNum(openPosition.getQuantity());
            tradingRecord.enter(0, price, qty);

        } else {
            AtomicInteger index = new AtomicInteger();
            aggOrders.forEach(order -> {
                int idx = index.getAndIncrement();
                Num price = toDecimalNum(order.getAvgFilledPrice());
                Num qty = toDecimalNum(order.getFilledQuantity());

                tradingRecord.operate(idx, price, qty);
            });
        }
        return tradingRecord;
    }

    private static void removeFirstIfSalle(List<Order> orders) {
        if (!CollectionUtils.isEmpty(orders) && orders.get(0).getAction().equals(OrderAction.SELL)) {
            orders.remove(0);
            log.warn("Removed first order from the list since it is a SELL action.");
        }
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

    //--------------------------------------------------------
    //-------------Private Methods----------------------------
    //--------------------------------------------------------

    private static void printTable(AsciiTable table) {
        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }

    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toRndBigDecimal(percent);
    }

//    private String getRange(Position it) {
//        return it.getEntry().getIndex() + "-" + it.getExit().getIndex();
//    }

//    private String getActionTime(int idx) {
//        final Bar bar = series.getBar(idx);
//        final ZonedDateTime endTime = bar.getEndTime();
//
//        return String.format("%s:%s", endTime.getHour(), endTime.getMinute());
//    }


//    private static String actionTime(Order order) {
//        ZonedDateTime time = Instant.ofEpochSecond(order.getFilledTime()).atZone(BarSeriesUtils.getDefaultZone());
//
//        return String.format("%s %s:%s", order.getAction(), time.getHour(), time.getMinute());
//    }


}
