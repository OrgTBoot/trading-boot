package com.mg.trading.boot.integrations.webull.data.common;

import lombok.*;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WTicker {
    private long tickerId;
    private String name;
    private String currencyCode;
    private String disExchangeCode;
    private String disSymbol;
    private String exchangeCode;
    private String regionCode;
    private String symbol;
    private String template;
    private String tinyName;
    private boolean oddLotSupport;
    private int type;
    private int regionId;
    private int currencyId;
    private int exchangeId;
    private int listStatus;
    private int derivativeSupport;
}
