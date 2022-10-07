package com.mg.trading.boot.integrations.yahoo.data;

import lombok.Data;

import java.util.List;

@Data
public class YahooChartResultIndicators {
    private List<YahooChartResultIndicatorsQuote> quote;
}
