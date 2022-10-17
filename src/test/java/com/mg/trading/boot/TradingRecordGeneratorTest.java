package com.mg.trading.boot;

import com.mg.trading.boot.models.Order;
import com.mg.trading.boot.models.OrderAction;
import com.mg.trading.boot.models.OrderStatus;
import com.mg.trading.boot.models.OrderType;
import com.mg.trading.boot.utils.TradingRecordUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Log4j2
public class TradingRecordGeneratorTest {

    @Test
    public void testAggregatedOrders() {
        List<Order> originalOrders = new ArrayList<>();

        originalOrders.add(Order.builder()
                .id("1")
                .action(OrderAction.BUY)
                .orderType(OrderType.LIMIT)
                .totalQuantity(BigDecimal.valueOf(5))
                .filledQuantity(BigDecimal.valueOf(5))
                .avgFilledPrice(BigDecimal.valueOf(2))
                .status(OrderStatus.FILLED)
                .build());

        originalOrders.add(Order.builder()
                .id("2")
                .action(OrderAction.BUY)
                .orderType(OrderType.LIMIT)
                .totalQuantity(BigDecimal.valueOf(10))
                .filledQuantity(BigDecimal.valueOf(10))
                .avgFilledPrice(BigDecimal.valueOf(3))
                .status(OrderStatus.FILLED)
                .build());

        originalOrders.add(Order.builder()
                .id("3")
                .action(OrderAction.SELL)
                .orderType(OrderType.LIMIT)
                .totalQuantity(BigDecimal.valueOf(7))
                .filledQuantity(BigDecimal.valueOf(7))
                .avgFilledPrice(BigDecimal.valueOf(7))
                .status(OrderStatus.FILLED)
                .build());

        List<Order> aggOrders = TradingRecordUtils.aggregateOrders(originalOrders);
        Assert.assertEquals(2, aggOrders.size());
        //agg buy record
        Assert.assertEquals("1_agg_2", aggOrders.get(0).getId());
        Assert.assertEquals(15, aggOrders.get(0).getTotalQuantity().doubleValue(), 0);
        Assert.assertEquals(15, aggOrders.get(0).getFilledQuantity().doubleValue(), 0);
        Assert.assertEquals(3, aggOrders.get(0).getAvgFilledPrice().doubleValue(), 0);
        //sell record
        Assert.assertEquals("3", aggOrders.get(1).getId());
        Assert.assertEquals(7, aggOrders.get(1).getTotalQuantity().doubleValue(), 0);
        Assert.assertEquals(7, aggOrders.get(1).getFilledQuantity().doubleValue(), 0);
        Assert.assertEquals(7, aggOrders.get(1).getAvgFilledPrice().doubleValue(), 0);
    }
}
