package com.mg.trading.boot.integrations.webull.data.common;

import lombok.*;

import java.util.List;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTickerData {
    private List<WTicker> data;
    private boolean hasMore;
}
