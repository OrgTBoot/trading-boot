package com.mg.trading.boot.integrations.webull.data.trading;

import com.mg.trading.boot.integrations.webull.data.common.WPosition;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTAccount {
    private WTAccountSummary accountSummaryVO;
    private WTAssetSummary assetSummaryVO;

    @Data
    public static class WTAccountSummary {
        private Long secAccountId;
        private String currency;
        private BigDecimal totalMarketValue;
        private BigDecimal longMarketValue;
        private BigDecimal shortMarketValue;
        private BigDecimal totalCashValue;
        private BigDecimal dayBuyingPower;
        private BigDecimal settledFunds;
        private BigDecimal unsettledFunds;
    }

    @Data
    public static class WTAssetSummary {
        private List<WPosition> positions;
    }
}
