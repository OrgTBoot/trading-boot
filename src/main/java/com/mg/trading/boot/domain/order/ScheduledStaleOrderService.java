package com.mg.trading.boot.domain.order;

import com.google.common.annotations.VisibleForTesting;
import com.mg.trading.boot.domain.models.Order;
import com.mg.trading.boot.integrations.AccountProvider;
import com.mg.trading.boot.integrations.BrokerProvider;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Log4j2
@Service
public class ScheduledStaleOrderService {
    private final BrokerProvider broker;
    private final Long orderTimeToLiveSec;

    public ScheduledStaleOrderService(final BrokerProvider broker, final @Value("${account.order.stale-ttl-sec}") Long orderTimeToLiveSec) {
        this.broker = broker;
        this.orderTimeToLiveSec = orderTimeToLiveSec;
    }


    @Scheduled(fixedRate = 10000)
    public void cancelStaleOrders() {
        AccountProvider account = broker.account();
        List<Order> orders = account.getOpenOrders();

        orders.forEach(order -> {
            if (isStaleOrder(order)) {
                log.info("Canceling stale {} {} order at price {}...", order.getAction(), order.getTicker().getSymbol(), order.getLmtPrice());
                cancelOrder(account, order);
            }
        });
    }


    @VisibleForTesting
    public boolean isStaleOrder(Order order) {
        Instant currentDateTime = Instant.now(Clock.system(BarSeriesUtils.getDefaultZone()));
        Instant orderCreatedDateTime = Instant.ofEpochMilli(order.getPlacedTime());

        return orderCreatedDateTime.plus(this.orderTimeToLiveSec, ChronoUnit.SECONDS).isBefore(currentDateTime);
    }

    @SneakyThrows
    private void cancelOrder(AccountProvider account, Order order) {
        account.cancelOrder(order.getId());

        for (int i = 0; i <= 60; i++) {
            List<Order> openOrders = account.getOpenOrders(order.getTicker().getSymbol());
            if (!containsOrder(openOrders, order)) {
                log.debug("Order {} {} cancellation confirmed.", order.getAction(), order.getTicker().getSymbol());
                return;
            }
            Thread.sleep(1000);
            log.debug("Waiting for order {} {} cancellation.", order.getAction(), order.getTicker().getSymbol());
        }
        log.error("Failed to cancel {} {} order.", order.getAction(), order.getTicker().getSymbol());
    }

    private boolean containsOrder(List<Order> orders, Order order) {
        return orders.stream().anyMatch(it -> it.getId().equalsIgnoreCase(order.getId()));
    }
}
