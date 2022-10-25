package com.mg.trading.boot.domain.rules;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;

import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.domain.rules.MarketTimeLeftRule.Market.MARKET_HOURS;
import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;
import static com.mg.trading.boot.tbd.TestDataProvider.buildBarSeries;

public class MarketTimeToExtendedHoursCloseRuleTest {

    @Test
    public void marketTimeToExtendedHoursCloseRuleTest() {
        BarSeries series = buildBarSeries();
        Rule rule = new MarketTimeLeftRule(series, MARKET_HOURS, 60, TimeUnit.MINUTES);

        series.addBar(buildBar(1, 0));  //pre
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(10, 0)); //mkt
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(12, 0)); //mkt
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(13, 0)); //aft
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(14, 0)); //aft
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(15, 0)); //aft - 3PM
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(16, 0)); //aft - 4PM
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(23, 0)); //aft - 11PM
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
    }
}
