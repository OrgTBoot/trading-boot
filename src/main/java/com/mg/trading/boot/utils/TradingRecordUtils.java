package com.mg.trading.boot.utils;

import com.mg.trading.boot.models.TradingMetrics;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.NumberOfLosingPositionsCriterion;
import org.ta4j.core.criteria.NumberOfWinningPositionsCriterion;
import org.ta4j.core.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;

import java.math.BigDecimal;

import static com.mg.trading.boot.integrations.utils.Mapper.toBigDecimal;

public class TradingRecordUtils {

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

}
