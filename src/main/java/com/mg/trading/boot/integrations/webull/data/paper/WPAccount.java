package com.mg.trading.boot.integrations.webull.data.paper;

import com.mg.trading.boot.integrations.webull.data.common.WOrder;
import com.mg.trading.boot.integrations.webull.data.common.WPosition;
import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WPAccount {
    private List<WOrder> openOrders;
    private List<WPosition> positions;
}
