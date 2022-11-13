package com.mg.trading.boot.domain.order;

import com.mg.trading.boot.domain.models.Order;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OrderManagementServiceTest {

    ScheduledStaleOrderService service = new ScheduledStaleOrderService(null, 30L);


    @Test
    public void testStaleOrderExpiredExactTime() {
        long filledTime = Instant.now().minus(30, ChronoUnit.SECONDS).toEpochMilli();
        Order order = Order.builder().placedTime(filledTime).build();

        Assert.assertTrue(service.isStaleOrder(order));
    }

    @Test
    public void testStaleOrderExpired() {
        long filledTime = Instant.now().minus(35, ChronoUnit.SECONDS).toEpochMilli();
        Order order = Order.builder().placedTime(filledTime).build();
        Assert.assertTrue(service.isStaleOrder(order));
    }


    @Test
    public void testStaleOrderNotExpired() {
        long filledTime = Instant.now().minus(29, ChronoUnit.SECONDS).toEpochMilli();
        Order order = Order.builder().placedTime(filledTime).build();
        Assert.assertFalse(service.isStaleOrder(order));
    }
}
