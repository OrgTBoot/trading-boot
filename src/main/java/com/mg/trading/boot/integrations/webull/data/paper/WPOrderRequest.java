package com.mg.trading.boot.integrations.webull.data.paper;

import com.mg.trading.boot.integrations.webull.data.common.WOrderAction;
import com.mg.trading.boot.integrations.webull.data.common.WOrderComboType;
import com.mg.trading.boot.integrations.webull.data.common.WOrderTimeInForce;
import com.mg.trading.boot.integrations.webull.data.common.WOrderType;
import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WPOrderRequest {
    private Long tickerId;
    private Boolean shortSupport;
    private WOrderType orderType;
    private WOrderTimeInForce timeInForce;
    private BigDecimal quantity;
    private Boolean outsideRegularTradingHour;
    private WOrderAction action;
    private BigDecimal lmtPrice;
    private WOrderComboType comboType;
    private String serialId;
    private WTEntrustType entrustType;
    private String assetType;
}
