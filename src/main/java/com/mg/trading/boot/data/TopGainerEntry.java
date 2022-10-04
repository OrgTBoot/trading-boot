//package com.mg.trading.boot.data;
//
//import com.mg.trading.boot.integrations.webull.data.TickerAnalysis;
//import com.mg.trading.boot.integrations.webull.data.TickerDetails;
//import com.mg.trading.boot.integrations.webull.data.TopGainersData;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Optional;
//
//
//@Data
//@Builder
//@ToString
//@AllArgsConstructor
//@NoArgsConstructor
//public class TopGainerEntry {
//
//    private Long id;
//    private MarketHours marketHours;
//    private String symbol;
//    private String name;
//    private BigDecimal marketCap;
//    private Long volume;
//    private Long avgVolume10D;
//    private Long avgVolume3M;
//    private BigDecimal turnover;
//    private String analysisRating;
//    private BigDecimal price;
//    private BigDecimal percentChange;
//    private BigDecimal percentChangeAfterMarket;
//    private BigDecimal totalPercentChange;
//    private BigDecimal low;
//    private BigDecimal high;
//    private BigDecimal takeProfitPrice;
//    private BigDecimal stopLossPrice;
//    private String estimatedEarningsDate;
//    private BigDecimal eps;
//
//
//    public static TopGainerEntry build(MarketHours marketHours,
//                                       TopGainersData.Entry dataEntry,
//                                       TickerDetails details,
//                                       TickerAnalysis analysis,
//                                       TopGainersCriteria.Parameters parameters) {
//        final TopGainerEntry entry = new TopGainerEntry();
//
//        entry.id = dataEntry.getTicker().getTickerId();
//        entry.marketHours = marketHours;
//        entry.name = dataEntry.getTicker().getName();
//        entry.symbol = dataEntry.getTicker().getSymbol();
//        entry.analysisRating = analysis.getRating().getRatingAnalysis();
//        entry.price = details.getClose();
//        entry.percentChange = dataEntry.getTicker().getChangeRatio().multiply(BigDecimal.valueOf(100));
////        entry.percentChange = dataEntry.getValues().getChangeRatio().multiply(BigDecimal.valueOf(100));
//        entry.percentChangeAfterMarket = Optional.ofNullable(dataEntry.getTicker().getPchRatio()).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(100));
//        entry.totalPercentChange = entry.percentChange.add(entry.percentChangeAfterMarket);
//        entry.marketCap = dataEntry.getTicker().getMarketValue();
//        entry.low = analysis.getTargetPrice().getLow();
//        entry.high = analysis.getTargetPrice().getHigh();
//        entry.volume = details.getVolume();
//        entry.avgVolume10D = details.getAvgVol10D();
//        entry.avgVolume3M = details.getAvgVol3M();
//        entry.turnover = dataEntry.getTicker().getTurnoverRate().multiply(BigDecimal.valueOf(100));
//        entry.estimatedEarningsDate = details.getEstimateEarningsDate();
//        entry.eps = details.getEps();
//
//        // ((intendedGain% / 100) * stockPrice ) + stockPrice
//        entry.takeProfitPrice = parameters.getTakeProfitPercent()
//                .divide(BigDecimal.valueOf(100), 2, RoundingMode.UP)
//                .multiply(details.getClose())
//                .add(details.getClose());
//
//        entry.stopLossPrice = details.getClose().subtract(parameters.getStopLossPercent()
//                .divide(BigDecimal.valueOf(100), 2, RoundingMode.UP)
//                .multiply(details.getClose()));
//
//        return entry;
//    }
//}
