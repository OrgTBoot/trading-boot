package com.mg.trading.boot.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private OrderAction action;
    private OrderType orderType;
    private OrderTimeInForce timeInForce;
    private BigDecimal totalQuantity;
    private BigDecimal filledQuantity;
    private BigDecimal lmtPrice;
    private BigDecimal avgFilledPrice;
    private String placedTime;
    private Long filledTime;
    private OrderStatus status;
    private Ticker ticker;
}
