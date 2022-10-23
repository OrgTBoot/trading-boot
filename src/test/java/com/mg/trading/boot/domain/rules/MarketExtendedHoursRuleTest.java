package com.mg.trading.boot.domain.rules;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;

import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;
import static com.mg.trading.boot.tbd.TestDataProvider.buildBarSeries;

public class MarketExtendedHoursRuleTest {

    @Test
    public void marketHoursRuleTest() {
        BarSeries series = buildBarSeries();
        Rule rule = new MarketExtendedHoursRule(series);

        series.addBar(buildBar(0, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(1, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(2, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(3, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        //pre-market
        series.addBar(buildBar(4, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(5, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(6, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(7, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(8, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(9, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(9, 29));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        //market
        series.addBar(buildBar(9, 31));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(13, 59));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(14, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(15, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        //after
        series.addBar(buildBar(16, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(17, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(18, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(19, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(19, 59));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        //after extended
        series.addBar(buildBar(20, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

        series.addBar(buildBar(21, 0));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
    }
}
