package com.mg.trading.boot.integrations.yahoo.data;

import lombok.Data;

import java.util.List;

@Data
public class YahooChart {
    private List<YahooChartResult> result;
}
