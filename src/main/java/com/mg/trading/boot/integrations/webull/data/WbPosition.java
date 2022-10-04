package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbPosition {
    private Long id;
    private Long accountId;
    private BigDecimal cost;
    private BigDecimal costPrice;
    private BigDecimal lastPrice;
    private BigDecimal position;
    private BigDecimal marketValue;

    private WbTicker ticker;
}
