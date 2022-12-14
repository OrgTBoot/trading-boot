package com.mg.trading.boot.domain.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String orderId;
    private String symbol;
    private OrderAction action;
    private OrderType orderType;
    private OrderTimeInForce timeInForce;
    private BigDecimal quantity;
    private BigDecimal lmtPrice;
}
