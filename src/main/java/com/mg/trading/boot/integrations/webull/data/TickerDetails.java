package com.mg.trading.boot.integrations.webull.data;

import lombok.Data;

import java.math.BigDecimal;

/**
 * avgVol3M: "2069701"
 * avgVol10D: "2491916"
 * baSize: 0
 * bps: "0.4814"
 * change: "2.87"
 * changeRatio: "0.4178"
 * close: "9.74"
 * currencyCode: "USD"
 * currencyId: 247
 * derivativeSupport: 1
 * disExchangeCode: "NASDAQ"
 * disSymbol: "SLDB"
 * eps: "-2.9095"
 * epsTtm: "-2.0537"
 * estimateEarningsDate: ""
 * exchangeCode: "NSQ"
 * exchangeId: 96
 * fiftyTwoWkHigh: "9.95"
 * fiftyTwoWkLow: "1.930"
 * forwardPe: "-5.6816"
 * high: "9.95"
 * indicatedPe: "-5.5350"
 * latestEarningsDate: "2020-11-05"
 * limitDown: "0.0000"
 * limitUp: "0.0000"
 * listStatus: 1
 * lotSize: "1"
 * low: "6.65"
 * marketValue: "588776581.34"
 * name: "SOLID BIOSCIENCE"
 * negMarketValue: "286821669.40"
 * ntvSize: 0
 * open: "6.75"
 * outstandingShares: "29447810"
 * pChRatio: "-0.0195"
 * pChange: "-0.19"
 * pPrice: "9.55"
 * pb: "20.23"
 * pe: "-3.3477"
 * peTtm: "-4.7427"
 * preClose: "6.87"
 * regionCode: "US"
 * regionId: 6
 * secType: [61]
 * status: "A"
 * symbol: "SLDB"
 * template: "stock"
 * tickerId: 925412848
 * timeZone: "America/New_York"
 * totalShares: "60449341"
 * tradeStatus: "D"
 * tradeTime: "2021-02-24T00:59:33.096+0000"
 * turnoverRate: "0.2141"
 * type: 2
 * tzName: "EST"
 * vibrateRatio: "0.4789"
 * volume: "12942145"
 * yield: "0.0000"
 */
@Data
public class TickerDetails {

    private long avgVol3M;
    private long avgVol10D;
    private long baSize;
    private BigDecimal bps;
    private BigDecimal change;
    private BigDecimal changeRatio;
    private BigDecimal close;
    private String currencyCode;
    private long currencyId;
    private long derivativeSupport;
    private String disExchangeCode;
    private String disSymbol;
    private BigDecimal eps;
    private BigDecimal epsTtm;
    private String estimateEarningsDate;
    private String exchangeCode;
    private long exchangeId;
    private BigDecimal fiftyTwoWkHigh;
    private BigDecimal fiftyTwoWkLow;
    private BigDecimal forwardPe;
    private BigDecimal high;
    private BigDecimal indicatedPe;
    private String latestEarningsDate;
    private BigDecimal limitDown;
    private BigDecimal limitUp;
    private long listStatus;
    private long lotSize;
    private BigDecimal low;
    private BigDecimal marketValue;
    private String name;
    private BigDecimal negMarketValue;
    private long ntvSize;
    private BigDecimal open;
    private long outstandingShares;
    private BigDecimal pChRatio;
    private BigDecimal pChange;
    private BigDecimal pPrice;
    private BigDecimal pb;
    private BigDecimal pe;
    private BigDecimal peTtm;
    private BigDecimal preClose;
    private String regionCode;
    private long regionId;
    private int[] secType;
    private String status;
    private String symbol;
    private String template;
    private long tickerId;
    private String timeZone;
    private long totalShares;
    private String tradeStatus;
    private String tradeTime;
    private BigDecimal turnoverRate;
    private long type;
    private String tzName;
    private BigDecimal vibrateRatio;
    private long volume;
    private BigDecimal yield;
}
