package com.mg.trading.boot.models;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private String id;
    private String accountId;
    private BigDecimal cost;
    private BigDecimal costPrice;
    private BigDecimal lastPrice;
    private BigDecimal marketValue;
    private BigDecimal quantity;
    private Ticker ticker;
}
