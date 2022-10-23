package com.mg.trading.boot.tbd;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Bar;

import static com.mg.trading.boot.tbd.TestDataProvider.buildBar;
import static com.mg.trading.boot.domain.rules.indicators.markethours.AbstractMarketHoursIndicator.*;

public class AbstractMarketHoursIndicatorTest {


    @Test
    public void preMarketHoursTest() {
        Assert.assertFalse(isPremarketHours(buildBar(1, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(2, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(3, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(3, 59)));

        Assert.assertTrue(isPremarketHours(buildBar(4, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(4, 5)));
        Assert.assertTrue(isPremarketHours(buildBar(5, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(6, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(7, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(8, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(9, 0)));
        Assert.assertTrue(isPremarketHours(buildBar(9, 29)));

        Assert.assertFalse(isPremarketHours(buildBar(9, 30)));
        Assert.assertFalse(isPremarketHours(buildBar(9, 35)));
        Assert.assertFalse(isPremarketHours(buildBar(10, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(11, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(12, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(13, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(14, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(15, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(16, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(17, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(18, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(19, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(20, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(22, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(23, 0)));
        Assert.assertFalse(isPremarketHours(buildBar(23, 59)));
    }

    @Test
    public void marketHoursTest() {
        Assert.assertFalse(isMarketHours(buildBar(1, 0)));
        Assert.assertFalse(isMarketHours(buildBar(2, 0)));
        Assert.assertFalse(isMarketHours(buildBar(3, 0)));
        Assert.assertFalse(isMarketHours(buildBar(4, 0)));
        Assert.assertFalse(isMarketHours(buildBar(5, 0)));
        Assert.assertFalse(isMarketHours(buildBar(6, 0)));
        Assert.assertFalse(isMarketHours(buildBar(7, 0)));
        Assert.assertFalse(isMarketHours(buildBar(8, 0)));
        Assert.assertFalse(isMarketHours(buildBar(9, 0)));
        Assert.assertFalse(isMarketHours(buildBar(9, 29)));

        Assert.assertTrue(isMarketHours(buildBar(9, 30)));
        Assert.assertTrue(isMarketHours(buildBar(10, 0)));
        Assert.assertTrue(isMarketHours(buildBar(11, 0)));
        Assert.assertTrue(isMarketHours(buildBar(12, 0)));
        Assert.assertTrue(isMarketHours(buildBar(13, 0)));
        Assert.assertTrue(isMarketHours(buildBar(14, 0)));
        Assert.assertTrue(isMarketHours(buildBar(15, 0)));
        Assert.assertTrue(isMarketHours(buildBar(15, 59)));

        Assert.assertFalse(isMarketHours(buildBar(16, 0)));
        Assert.assertFalse(isMarketHours(buildBar(16, 5)));
        Assert.assertFalse(isMarketHours(buildBar(17, 0)));
        Assert.assertFalse(isMarketHours(buildBar(18, 0)));
        Assert.assertFalse(isMarketHours(buildBar(19, 0)));
        Assert.assertFalse(isMarketHours(buildBar(20, 0)));
        Assert.assertFalse(isMarketHours(buildBar(22, 0)));
        Assert.assertFalse(isMarketHours(buildBar(23, 0)));
        Assert.assertFalse(isMarketHours(buildBar(23, 59)));
    }

    @Test
    public void afterMarketHoursTest() {
        Assert.assertFalse(isExtendedMarketHours(buildBar(1, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(2, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(3, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(4, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(5, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(6, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(7, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(8, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(9, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(9, 29)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(9, 30)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(10, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(11, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(12, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(13, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(14, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(15, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(15, 59)));

        Assert.assertTrue(isExtendedMarketHours(buildBar(16, 0)));
        Assert.assertTrue(isExtendedMarketHours(buildBar(16, 5)));
        Assert.assertTrue(isExtendedMarketHours(buildBar(17, 0)));
        Assert.assertTrue(isExtendedMarketHours(buildBar(18, 0)));
        Assert.assertTrue(isExtendedMarketHours(buildBar(19, 0)));
        Assert.assertTrue(isExtendedMarketHours(buildBar(19, 59)));

        Assert.assertFalse(isExtendedMarketHours(buildBar(20, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(22, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(23, 0)));
        Assert.assertFalse(isExtendedMarketHours(buildBar(23, 59)));
    }

    @Test
    public void getMinutesTillExtendedMarketHoursCloseTest() {
        Bar bar = buildBar(15, 0);
        Assert.assertEquals(60, getMinutesTillMarketExtendedHoursClose(bar));

        bar = buildBar(15, 30);
        Assert.assertEquals(30, getMinutesTillMarketExtendedHoursClose(bar), 0);
    }
}
