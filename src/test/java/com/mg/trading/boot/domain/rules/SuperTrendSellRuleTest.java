package com.mg.trading.boot.domain.rules;

import com.mg.trading.boot.domain.indicators.supertrentv2.Signal;
import com.mg.trading.boot.domain.indicators.supertrentv2.Trend;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;

import java.math.BigDecimal;

import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;

public class SuperTrendSellRuleTest {

    @Test
    public void superTrendSellRuleTest_upTrend() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(9, 59, BigDecimal.valueOf(0.99)));
        series.addBar(buildBar(10, 0, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 1, BigDecimal.valueOf(2)));
        series.addBar(buildBar(10, 2, BigDecimal.valueOf(3)));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(4)));
        series.addBar(buildBar(10, 4, BigDecimal.valueOf(5)));
        series.addBar(buildBar(10, 5, BigDecimal.valueOf(6)));
        series.addBar(buildBar(10, 6, BigDecimal.valueOf(7)));
        series.addBar(buildBar(10, 7, BigDecimal.valueOf(8)));
        series.addBar(buildBar(10, 8, BigDecimal.valueOf(9)));
        series.addBar(buildBar(10, 9, BigDecimal.valueOf(10)));

        Rule rule = new SuperTrendRule(series, 10, Trend.DOWN, Signal.DOWN);
        Assert.assertFalse(rule.isSatisfied(series.getEndIndex()));
    }

    /**
     * On sideways market it will sell
     */
    @Test
    public void superTrendSellRuleTest_sidewaysTrend() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 1, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 2, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 4, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 5, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 6, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 7, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 8, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 9, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 10, BigDecimal.valueOf(1)));

        Rule rule = new SuperTrendRule(series, 10, Trend.DOWN, Signal.DOWN);
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
    }

    @Test
    public void superTrendSellRuleTest_downTrend() {
        BaseBarSeries series = new BaseBarSeries();
        series.addBar(buildBar(10, 0, BigDecimal.valueOf(1)));
        series.addBar(buildBar(10, 1, BigDecimal.valueOf(0.9)));
        series.addBar(buildBar(10, 2, BigDecimal.valueOf(0.8)));
        series.addBar(buildBar(10, 3, BigDecimal.valueOf(0.7)));
        series.addBar(buildBar(10, 4, BigDecimal.valueOf(0.6)));
        series.addBar(buildBar(10, 5, BigDecimal.valueOf(0.5)));
        series.addBar(buildBar(10, 6, BigDecimal.valueOf(0.4)));
        series.addBar(buildBar(10, 7, BigDecimal.valueOf(0.3)));
        series.addBar(buildBar(10, 8, BigDecimal.valueOf(0.2)));
        series.addBar(buildBar(10, 9, BigDecimal.valueOf(0.15)));
        series.addBar(buildBar(10, 10, BigDecimal.valueOf(0.1)));

        Rule rule = new SuperTrendRule(series, 10, Trend.DOWN, Signal.DOWN);
        Assert.assertTrue(rule.isSatisfied(series.getEndIndex()));
    }
}
