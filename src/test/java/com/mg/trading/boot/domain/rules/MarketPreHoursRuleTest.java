package com.mg.trading.boot.domain.rules;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;

import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;
import static com.mg.trading.boot.tbd.TestDataProvider.buildBarSeries;

public class MarketPreHoursRuleTest {

    @Test
    public void marketPreHoursRuleTest() {
        BarSeries series = buildBarSeries();
        Rule rule = new MarketPreHoursRule(series);

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
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(5, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(6, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(7, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(8, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(9, 0));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
        series.addBar(buildBar(9, 29));
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));

        //market
        series.addBar(buildBar(9, 31));
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));

    }
}
