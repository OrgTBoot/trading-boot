package com.mg.trading.boot.utils;

import com.mg.trading.boot.data.TradingMetrics;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.util.CollectionUtil;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.*;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.mg.trading.boot.integrations.utils.Mapper.toBigDecimal;

@Log4j2
public class TradingRecordUtils {

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

    public static TradingMetrics buildTradingMetrics(String symbol, BarSeries series, TradingRecord tradingRecord) {
        if (tradingRecord == null || CollectionUtils.isEmpty(tradingRecord.getPositions())) {
            return TradingMetrics.builder().build();
        }
        BigDecimal winingPositionsRatio = toBigDecimal(new WinningPositionsRatioCriterion().calculate(series, tradingRecord));
//        BigDecimal riskRewardRatio = toBigDecimal(new ReturnOverMaxDrawdownCriterion().calculate(series, tradingRecord));
        BigDecimal totalReturnVsBuyHold = toBigDecimal(new VersusBuyAndHoldCriterion(new GrossReturnCriterion()).calculate(series, tradingRecord));
        BigDecimal totalReturn = toBigDecimal(new ProfitLossCriterion().calculate(series, tradingRecord));
        BigDecimal totalReturnPercent = toBigDecimal(new ProfitLossPercentageCriterion().calculate(series, tradingRecord));
        Integer totalWinningPositions = new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).intValue();
        Integer totalLoosingPositions = new NumberOfLosingPositionsCriterion().calculate(series, tradingRecord).intValue();
//        BigDecimal totalReturnPercent = getTradingRecordTotalProfit(tradingRecord);

        return TradingMetrics.builder()
                .symbol(symbol)
                .totalReturn(totalReturn)
                .totalPercentReturn(totalReturnPercent)
                .winningPositionsRatio(winingPositionsRatio)
                .totalReturnVsBuyAndHold(totalReturnVsBuyHold)
                .totalPositions(tradingRecord.getPositions().size())
                .totalWinningPositions(totalWinningPositions)
                .totalLoosingPositions(totalLoosingPositions)
//                .riskRewardRatio(riskRewardRatio)
                .build();
    }


    private static BigDecimal getPositionProfitInPercent(Position position) {
        Num percent = new ProfitLossPercentageCriterion().calculate(new BaseBarSeries(), position);
        return toBigDecimal(percent);
    }


    private static BigDecimal defaultToZero(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

}
