package com.mg.trading.boot.integrations.webull.data.trading;

import com.mg.trading.boot.integrations.webull.data.common.WTicker;
import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTItem {
    private WTicker ticker;
}
