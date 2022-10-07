package com.mg.trading.boot.integrations.yahoo.data;

import lombok.Data;

@Data
public class YahooChartResultMeta {
    private String symbol;
    private String instrumentType;
    private String currency;
    private String dataGranularity;
    private String range;
    private String timezone;
    private String exchangeTimezoneName;
}
