package com.mg.trading.boot.integrations.webull.data.common;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WOrder {
    private long orderId;
    private WOrderAction action;
    private String totalQuantity;
    private String filledQuantity;
    private String placedTime;
    private WOrderTimeInForce timeInForce;
    private WOrderType orderType;
    private String lmtPrice;
    private String avgFilledPrice;
    private Instant createTime0;
    private Instant filledTime0;
    private WOrderStatus status;
    private WOrderStatus statusCode;
    private WTicker ticker;
}
