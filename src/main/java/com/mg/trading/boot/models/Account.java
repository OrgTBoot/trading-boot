package com.mg.trading.boot.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String id;
    private String currency;
    private BigDecimal totalMarketValue;
    private BigDecimal longMarketValue;
    private BigDecimal shortMarketValue;
    private BigDecimal totalCash;
    private BigDecimal buyingPower;
    private BigDecimal settledFunds;
    private BigDecimal unsettledFunds;
    private List<Order> openOrders;
    private List<Position> positions;
}
