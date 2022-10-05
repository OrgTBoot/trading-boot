//package com.mg.trading.boot.data;
//
//import lombok.Data;
//import lombok.ToString;
//
//import java.math.BigDecimal;
//
//@Data
//@ToString
//public class TopGainersCriteria {
//
//    private Filter filter = new Filter();
//    private Query query = new Query();
//    private Parameters parameters = new Parameters();
//
//    @Data
//    public static class Filter {
//        private BigDecimal minMarketCap = BigDecimal.valueOf(100000000);
//    }
//
//    @Data
//    public static class Query {
//        private MarketHours marketHours;
//    }
//
//    @Data
//    public static class Parameters {
//        private BigDecimal takeProfitPercent;
//        private BigDecimal stopLossPercent;
//    }
//}
