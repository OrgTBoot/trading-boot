package com.mg.trading.boot.integrations.webull.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
 *{
		"ticker": {
			"tickerId": 913255341,
			"exchangeId": 11,
			"type": 2,
			"secType": [61],
			"regionId": 6,
			"currencyId": 247,
			"currencyCode": "USD",
			"name": "GameStop",
			"symbol": "GME",
			"disSymbol": "GME",
			"disExchangeCode": "NYSE",
			"exchangeCode": "NYSE",
			"listStatus": 1,
			"template": "stock",
			"derivativeSupport": 1,
			"tradeTime": "2021-02-25T00:59:59.826+0000",
			"faTradeTime": "2021-02-25T00:59:59.826+0000",
			"status": "A",
			"close": "91.71",
			"change": "46.74",
			"changeRatio": "1.0394",
			"marketValue": "6396493701",
			"volume": "83111740",
			"turnoverRate": "1.1916",
			"regionName": "United States",
			"regionIsoCode": "US",
			"peTtm": "-21.9602",
			"preClose": "44.97",
			"fiftyTwoWkHigh": "483.00",
			"fiftyTwoWkLow": "2.570",
			"open": "44.70",
			"high": "91.71",
			"low": "44.70",
			"vibrateRatio": "1.045",
			"pprice": "168.00",
			"pchange": "76.29",
			"pchRatio": "0.8319"
		},
		"values": {
			"tickerId": 913255341,
			"change": "46.74",
			"changeRatio": "1.0394"
		}
	}
 */
@Data
public class TopGainersData {

    private String rankType;
    private int direction;
    private boolean hasMore;
    private List<Entry> data = new ArrayList<>();

    @Data
    public static class Entry {
        private Ticker ticker;
        private Values values;
    }

    @Data
    public static class Ticker {

        private long tickerId;
        private long exchangeId;
        private int type;
        private int[] secType;
        private int regionId;
        private int currencyId;
        private int listStatus;
        private int derivativeSupport;
        private String currencyCode;
        private String name;
        private String symbol;
        private String disSymbol;
        private String disExchangeCode;
        private String exchangeCode;
        private String template;
        private String tradeTime;
        private String faTradeTime;
        private String status;
        private String regionName;
        private String regionIsoCode;
        private BigDecimal close;
        private BigDecimal change;
        private BigDecimal changeRatio;
        private BigDecimal marketValue;
        private BigDecimal volume;
        private BigDecimal turnoverRate;
        private BigDecimal peTtm;
        private BigDecimal preClose;
        private BigDecimal fiftyTwoWkHigh;
        private BigDecimal fiftyTwoWkLow;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal vibrateRatio;
        private BigDecimal pprice;
        private BigDecimal pchange;
        private BigDecimal pchRatio;
    }

    @Data
    public static class Values {
        private long tickerId;
        private BigDecimal change;
        private BigDecimal changeRatio;
    }
}
