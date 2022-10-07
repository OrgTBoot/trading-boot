package com.mg.trading.boot.integrations.yahoo.data;

import lombok.Data;

import java.util.List;

@Data
public class YahooChartResult {
    private YahooChartResultMeta meta;
    private List<Long> timestamp;
    private YahooChartResultIndicators indicators;
}
