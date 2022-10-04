package com.mg.trading.boot.data;

import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TickerQuote {
    String timeZone;
    Long timeStamp;
    Long volume;
    BigDecimal openPrice;
    BigDecimal highPrice;
    BigDecimal lowPrice;
    BigDecimal closePrice;
}
