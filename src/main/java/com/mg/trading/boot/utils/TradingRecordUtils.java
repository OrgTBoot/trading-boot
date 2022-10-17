package com.mg.trading.boot.utils;

import com.mg.trading.boot.models.Order;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TradingRecordUtils {

    /**
     * Aggregates orders that have more than one same consecutive action.
     * For example a list of orders [BUY, BUY, SELL, BUY] will result in an aggregated list [BUY, SELL, BUY]
     *
     * @param orders - orders for aggregation
     * @return - aggregated list of orders
     */
    public static List<Order> aggregateOrders(List<Order> orders) {
        List<Order> aggRecords = new ArrayList<>();

        for (Order current : orders) {
            Order prev = CollectionUtils.isEmpty(aggRecords) ? null : aggRecords.get(aggRecords.size() - 1);

            if (prev == null) {
                aggRecords.add(current);
                continue;
            }

            boolean sameAction = prev.getAction().equals(current.getAction());
            if (!sameAction) {
                aggRecords.add(current);
            } else {
                Order aggOrder = Order.builder()
                        .id(prev.getId() + "_agg_" + current.getId())
                        .action(prev.getAction())
                        .status(prev.getStatus())
                        .ticker(prev.getTicker())
                        .orderType(prev.getOrderType())
                        .lmtPrice(current.getLmtPrice())
                        .placedTime(current.getPlacedTime())
                        .filledTime(current.getFilledTime())
                        .timeInForce(current.getTimeInForce())
                        .totalQuantity(prev.getTotalQuantity().add(current.getTotalQuantity()))
                        .filledQuantity(prev.getFilledQuantity().add(current.getFilledQuantity()))
                        .avgFilledPrice(prev.getAvgFilledPrice().add(current.getAvgFilledPrice()).divide(BigDecimal.valueOf(2), RoundingMode.CEILING))
                        .build();

                int lastIdx = aggRecords.size() - 1;
                aggRecords.remove(lastIdx);
                aggRecords.add(aggOrder);
            }
        }
        return aggRecords;
    }
}
