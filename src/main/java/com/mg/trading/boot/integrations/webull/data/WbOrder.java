package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbOrder {
    private long orderId;
    private WbOrderAction action;
    private String totalQuantity;
    private String filledQuantity;
    private String placedTime;
    private WbOrderTimeInForce timeInForce;
    private WbOrderType orderType;
    private String lmtPrice;
    private String avgFilledPrice;
    private Instant createTime0;
    private Instant filledTime0;
    private WbOrderStatus status;
    private WbTicker ticker;
}
