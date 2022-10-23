package com.mg.trading.boot.domain.rules;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;

public class StopTotalLossRuleTest {
    Num oneShare = DecimalNum.valueOf(1);

    @Test
    public void stopTotalLossRuleTest_nullTradingRecord() {
        StopTotalLossRule rule = new StopTotalLossRule(null, BigDecimal.TEN.negate());
        Assert.assertFalse(rule.isSatisfied(0, null));
    }

    @Test
    public void stopTotalLossRuleTest_emptyTradingRecord() {
        TradingRecord tradingRecord = new BaseTradingRecord();
        StopTotalLossRule rule = new StopTotalLossRule(new BaseBarSeries(), BigDecimal.TEN.negate());

        Assert.assertFalse(rule.isSatisfied(0, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_oneBuyTradingRecord_noPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.TEN));
        series.addBar(buildBar(10, 1, BigDecimal.TEN));

        //open buy position at bar idx 0
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 1
        Assert.assertFalse(rule.isSatisfied(0, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_oneBuyTradingRecord_gainPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.ONE));
        series.addBar(buildBar(10, 1, BigDecimal.TEN)); //GAIN 900%

        //open buy position at bar idx 0
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 1
        Assert.assertFalse(rule.isSatisfied(1, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_closedGainTradingRecord() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.ONE));
        series.addBar(buildBar(10, 1, BigDecimal.TEN)); //GAIN 900%
        series.addBar(buildBar(10, 2, BigDecimal.valueOf(20))); //GAIN 900%

        //open buy/sel position at bars 0-1
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(1).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 2
        Assert.assertFalse(rule.isSatisfied(2, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_closedLossTradingRecord() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.TEN));
        series.addBar(buildBar(10, 1, BigDecimal.ONE)); //LOSS 900%
        series.addBar(buildBar(10, 2, BigDecimal.ONE));

        //open buy/sel position at bars 0-1
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(1).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 3
        Assert.assertTrue(rule.isSatisfied(3, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_closedGainTradingRecord_buy_gainPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.ONE));
        series.addBar(buildBar(10, 1, BigDecimal.TEN)); //GAIN 900%
        series.addBar(buildBar(10, 2, BigDecimal.TEN));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(20))); //GAIN 900%

        //open buy/sel position at bars 0-1
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(1).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(2).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 2
        Assert.assertFalse(rule.isSatisfied(3, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_closedLossTradingRecord_buy_gainPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.TEN));
        series.addBar(buildBar(10, 1, BigDecimal.ONE));
        series.addBar(buildBar(10, 2, BigDecimal.TEN));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(21)));

        //open buy/sel position at bars 0-1
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(1).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(2).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 3
        Assert.assertFalse(rule.isSatisfied(3, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_closedLossTradingRecord_buy_lossPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.ONE));
        series.addBar(buildBar(10, 1, BigDecimal.valueOf(0.95)));
        series.addBar(buildBar(10, 2, BigDecimal.ONE));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(0.5)));

        //open buy/sel position at bars 0-1
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(1).getClosePrice(), oneShare);
        tradingRecord.operate(0, series.getBar(2).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 3
        Assert.assertTrue(rule.isSatisfied(3, tradingRecord));
    }

    @Test
    public void stopTotalLossRuleTest_oneBuyTradingRecord_lossPriceChange() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.TEN));
        series.addBar(buildBar(10, 1, BigDecimal.ONE)); //LOSS -90%

        //open buy position at bar idx 0
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.operate(0, series.getBar(0).getClosePrice(), oneShare);

        StopTotalLossRule rule = new StopTotalLossRule(series, BigDecimal.TEN.negate());

        //evaluate at bar idx 1
        Assert.assertTrue(rule.isSatisfied(1, tradingRecord));
    }

}
