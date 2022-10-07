package com.mg.trading.boot.integrations.yahoo.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class YahooChartResultIndicatorsQuote {
    private List<Long> volume;
    private List<BigDecimal> low;
    private List<BigDecimal> close;
    private List<BigDecimal> open;
    private List<BigDecimal> high;
}
