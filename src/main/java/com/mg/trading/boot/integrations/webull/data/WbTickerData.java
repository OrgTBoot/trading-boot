package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.util.List;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbTickerData {
    private List<WbTicker> data;
    private boolean hasMore;
}
