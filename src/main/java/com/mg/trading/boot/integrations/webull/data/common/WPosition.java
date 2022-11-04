package com.mg.trading.boot.integrations.webull.data.common;

import com.mg.trading.boot.integrations.webull.data.trading.WTItem;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WPosition {
    private String id;
    private BigDecimal cost;
    private BigDecimal costPrice;
    private BigDecimal lastPrice;
    private BigDecimal marketValue;
    private BigDecimal position;
    private BigDecimal quantity;
    private String tickerType;
    private String unrealizedProfitLoss;
    private String unrealizedProfitLossBase;
    private String unrealizedProfitLossRate;
    private List<WTItem> items;


    private WTicker ticker;
}
