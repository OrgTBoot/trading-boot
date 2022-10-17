package com.mg.trading.boot.integrations.webull.data.common;

import lombok.*;

import java.util.List;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTickerQuote {
    private String tickerId;
    private String timeZone;
    private List<String> data;
}
