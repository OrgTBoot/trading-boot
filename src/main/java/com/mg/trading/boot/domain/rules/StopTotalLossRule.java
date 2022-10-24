package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.utils.NumberUtils;
import lombok.extern.log4j.Log4j2;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkState;

/**
 * Rule is considered satisfied if total loss of closed positions plus current open position exceeds the loss threshold.
 */
@Log4j2
public class StopTotalLossRule extends AbstractRule implements Rule {
    private final BarSeries series;
    private final BigDecimal lossThreshold;

    public StopTotalLossRule(BarSeries series, BigDecimal lossThreshold) {
        this.series = series;
        this.lossThreshold = NumberUtils.toNegative(lossThreshold);
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (tradingRecord == null) {
            traceIsSatisfied(index, false);
            return false;
        }

        Num closedPositionsLoss = new ProfitLossPercentageCriterion().calculate(series, tradingRecord);
        Num openPositionLoss = getCurrentPositionLoss(index, tradingRecord);
        Num totalLoss = closedPositionsLoss.plus(openPositionLoss);

        boolean satisfied = totalLoss.doubleValue() < lossThreshold.doubleValue();

        if (satisfied) {
            log.warn("You've reached total loss tolerance threshold. Positions: closed='{}', open='{}', total='{}'. Allowed is {}. Idx={}", closedPositionsLoss.doubleValue(), openPositionLoss.doubleValue(), totalLoss.doubleValue(), lossThreshold.doubleValue(), index);
        }
        traceIsSatisfied(index, satisfied);

        return satisfied;
    }


    private Num getCurrentPositionLoss(int idx, TradingRecord tradingRecord) {
        if (tradingRecord != null) {
            Position currentPosition = tradingRecord.getCurrentPosition();
            if (currentPosition.isOpened()) {

                if (currentPosition.getEntry().isBuy()) {
                    Num hundred = DecimalNum.valueOf(100);
                    Num entryPrice = currentPosition.getEntry().getNetPrice();
                    Num currentPrice = series.getBar(idx).getClosePrice();
                    Num priceDiff = currentPrice.minus(entryPrice);

                    return priceDiff.dividedBy(entryPrice).multipliedBy(hundred);
                }
            }
        }
        return DecimalNum.valueOf(0);
    }
}
