//package com.mg.trading.boot.utils;
//
//import de.vandermeer.asciitable.AT_Row;
//import de.vandermeer.asciitable.AsciiTable;
//import de.vandermeer.asciitable.CWC_LongestLine;
//import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
//import lombok.extern.log4j.Log4j2;
//import org.ta4j.core.BarSeries;
//import org.ta4j.core.BaseBarSeries;
//import org.ta4j.core.Position;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.reports.TradingStatement;
//import org.ta4j.core.reports.TradingStatementGenerator;
//
//import java.math.BigDecimal;
//import java.util.stream.IntStream;
//
//import static com.mg.trading.boot.utils.NumberUtils.toRndBigDecimal;
//
//@Log4j2
//public class ConsoleUtils {
//
//    public static TradingStatement buildTradingStatement(TradingRecord tradingRecord, BarSeries series) {
//        return new TradingStatementGenerator().generate(null, tradingRecord, series);
//    }
//
//    public static void printTradingStatement(String symbol,
//                                             TradingRecord tradingRecord,
//                                             BarSeries series) {
//        TradingStatement statement = buildTradingStatement(tradingRecord, series);
//        printTradingStatement(symbol, statement);
//    }
//
//
//
//}
