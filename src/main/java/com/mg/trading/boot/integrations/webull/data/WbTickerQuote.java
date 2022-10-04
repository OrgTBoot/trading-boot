package com.mg.trading.boot.integrations.webull.data;

import lombok.*;

import java.util.List;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WbTickerQuote {
    private String tickerId;
    private String timeZone;
    private List<String> data;
}
