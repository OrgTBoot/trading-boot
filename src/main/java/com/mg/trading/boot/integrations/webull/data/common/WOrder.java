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
    private Long createTime0;
    private Long filledTime0;
    private String filledTime;
    private WOrderStatus status;
    private WOrderStatus statusCode;
    private WTicker ticker;
}
