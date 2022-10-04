package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbAccount {
    private List<WbOrder> openOrders;
    private List<WbPosition> positions;
}
