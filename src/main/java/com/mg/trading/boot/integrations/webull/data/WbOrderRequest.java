package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbOrderRequest {
    private Long tickerId;
    private Boolean shortSupport;
    private WbOrderType orderType;
    private WbOrderTimeInForce timeInForce;
    private BigDecimal quantity;
    private Boolean outsideRegularTradingHour;
    private WbOrderAction action;
    private BigDecimal lmtPrice;
    private WbOrderComboType comboType;
    private String serialId;
}
