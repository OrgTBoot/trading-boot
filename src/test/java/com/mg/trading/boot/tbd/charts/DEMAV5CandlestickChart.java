package com.mg.trading.boot.tbd.charts;

import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.strategy.dema5.DEMAStrategyDefinitionV5_1;
import com.mg.trading.boot.tbd.TestDataProvider;
import org.jfree.data.time.TimeSeriesCollection;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import static com.mg.trading.boot.tbd.charts.CandlestickChart.buildChartBarSeries;

/**
 * This class builds a traditional candlestick chart.
 */
public class DEMAV5CandlestickChart {


    public static void main(String[] args) {
        String symbol = "COSM";
        String fileName = "tmp/COSM.json";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortDEMA = new DoubleEMAIndicator(closePrice, 10);
        DoubleEMAIndicator longDEMA = new DoubleEMAIndicator(closePrice, 60);
        BollingerBandFacade bollinger = new BollingerBandFacade(series, 60, 3.5);
        SuperTrend superTrend = new SuperTrend(series, 10, 3D);

        dataset.addSeries(buildChartBarSeries(series, shortDEMA, "S_DEMA"));
        dataset.addSeries(buildChartBarSeries(series, longDEMA, "L_DEMA"));
        dataset.addSeries(buildChartBarSeries(series, bollinger.upper(), "B_H"));
        dataset.addSeries(buildChartBarSeries(series, bollinger.lower(), "B_L"));
        dataset.addSeries(buildChartBarSeries(series, superTrend, "ST"));


        CandlestickChart chart = new CandlestickChart();
        chart.display(series, dataset, new DEMAStrategyDefinitionV5_1(symbol));
    }

}
