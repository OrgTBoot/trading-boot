package com.mg.trading.boot.integrations.webull.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * {
 * "rating": {
 * "ratingAnalysisTotals": 4,
 * "ratingAnalysis": "buy",
 * "ratingSpread": {
 * "underPerform": 0,
 * "buy": 2,
 * "sell": 0,
 * "strongBuy": 0,
 * "hold": 2
 * }* 	},
 * "targetPrice": {
 * "low": "5.60",
 * "high": "10.00",
 * "current": "7.99",
 * "mean": "7.20"* 	},
 * "forecastEps": {
 * "id": "EPS",
 * "title": "EPS",
 * "currencyName": "USD",
 * "points": [{
 * "xAxis": "Q4 2019",
 * "valueActual": -0.87,
 * "valueForecast": -0.52
 * }, {
 * "xAxis": "Q1 2020",
 * "valueActual": -0.25,
 * "valueForecast": -0.23
 * }, {
 * "xAxis": "Q2 2020",
 * "valueActual": -0.31,
 * "valueForecast": -0.8
 * }, {
 * "xAxis": "Q3 2020",
 * "valueActual": -0.38,
 * "valueForecast": -0.24
 * }, {
 * "xAxis": "Q4 2020",
 * "valueForecast": -0.2
 * }]* 	}
 * }
 */
@Data
public class TickerAnalysis {

    private Rating rating;
    private TargetPrice targetPrice;
    private ForecastEps forecastEps;

    @Data
    public static class Rating {
        private int ratingAnalysisTotals;
        private String ratingAnalysis;
        private RatingSpread ratingSpread;

        @Data
        static class RatingSpread {
            private int underPerform;
            private int buy;
            private int sell;
            private int strongBuy;
            private int hold;
        }
    }

    @Data
    public static class TargetPrice {
        private BigDecimal low;
        private BigDecimal high;
        private BigDecimal current;
        private BigDecimal men;
    }

    @Data
    static class ForecastEps {
        private String id;
        private String title;
        private String currencyName;
        private List<Points> points;

        @Data
        static class Points {
            private String xAxis;
            private BigDecimal valueActual;
            private BigDecimal valueForecast;
        }
    }
}
