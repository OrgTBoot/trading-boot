package com.mg.trading.boot.integrations.webull.data.paper;

import com.mg.trading.boot.integrations.webull.data.common.WOrder;
import com.mg.trading.boot.integrations.webull.data.common.WPosition;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WPAccount {
    private BigDecimal netLiquidation;
    private List<WPAccountMember> accountMembers;
    private List<WOrder> openOrders;
    private List<WPosition> positions;
}
