package com.mg.trading.boot.utils;

import com.mg.trading.boot.models.TradingMetrics;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.mg.trading.boot.integrations.utils.Mapper.toBigDecimal;

@Log4j2
public class ConsoleUtils {

    public static void printTradingRecords(String symbol, TradingRecord tradingRecord) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("ENTER IDX", "EXIT IDX", "SYMBOL", "SHARES", "ENTER PRICE", "EXIT PRICE", "PROFIT $", "PROFIT %").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        tradingRecord.getPositions().forEach(it -> {
            AT_Row row = table.addRow(
                    it.getEntry().getIndex(),
                    it.getExit().getIndex(),
                    symbol,
                    it.getEntry().getAmount(),
                    it.getEntry().getNetPrice(),
                    it.getExit().getNetPrice(),
                    it.getProfit(),
                    getPositionProfitInPercent(it) + "%");


            //align numbers to the left
            IntStream.of(3, 4, 5, 6, 7).forEach(colIdx -> row.getCells().get(colIdx).getContext().setTextAlignment(TextAlignment.RIGHT));
            table.addRule();
        });

        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }

    public static void printTradingMetrics(String symbol, BarSeries series, TradingRecord tradingRecord) {
        TradingMetrics metrics = TradingRecordUtils.buildTradingMetrics(symbol, series, tradingRecord);
        printTradingMetrics(metrics);
    }

    public static void printTradingMetrics(TradingMetrics metrics) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("SYMBOL", "METRIC", "VALUE").setTextAlignment(TextAlignment.CENTER);
        table.addRule();
        table.addRow(metrics.getSymbol(), "Total return", defaultToZero(metrics.getTotalPercentReturn()) + "% | " + defaultToZero(metrics.getTotalReturn()) + "$");
        table.addRule();
        table.addRow(metrics.getSymbol(), "Winning positions ratio", defaultToZero((metrics.getWinningPositionsRatio())));
        table.addRule();
//        table.addRow(metrics.getSymbol(), "Risk-reward ratio", defaultToZero(metrics.getRiskRewardRatio()));
//        table.addRule();
        table.addRow(metrics.getSymbol(), "Total positions | ↑ wins | ↓ losses ",
                metrics.getTotalPositions() + " | ↑" + metrics.getTotalWinningPositions() + " | ↓" + metrics.getTotalLoosingPositions());
        table.addRule();
//        table.addRow(metrics.getSymbol(), "Total return vs buy-and-hold return", defaultToZero(metrics.getTotalReturnVsBuyAndHold()));
//        table.addRule();

        table.getRenderer().setCWC(new CWC_LongestLine());
        log.info("\n" + table.render());
    }


    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toBigDecimal(percent);
    }


    private static BigDecimal defaultToZero(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

}
