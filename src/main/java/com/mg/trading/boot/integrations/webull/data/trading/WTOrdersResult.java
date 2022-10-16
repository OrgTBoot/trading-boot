package com.mg.trading.boot.integrations.webull.data.trading;

import com.mg.trading.boot.integrations.webull.data.common.WOrder;
import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTOrdersResult {
    private List<WOrder> items;
}
