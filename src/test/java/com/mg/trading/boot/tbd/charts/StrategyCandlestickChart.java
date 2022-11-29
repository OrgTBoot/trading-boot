package com.mg.trading.boot.tbd.charts;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.strategy.dema6_1.DEMAStrategyDefinitionV6_1;
import com.mg.trading.boot.tbd.TestDataProvider;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import static com.mg.trading.boot.tbd.charts.CandlestickChart.buildChartBarSeries;

/**
 * This class builds a traditional candlestick chart.
 */
public class StrategyCandlestickChart {


    public static void main(String[] args) {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);

        String symbol = "Stock";
//        String fileName = "tmp/10_24_2022_FRPT.json";
//        String fileName = "tmp/AMD.json";
        String fileName = "11_28_2022/AMD.json";
//        String fileName = "tmp/TNA.json";
//        String fileName = "tmp/AXSM.json";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator shortDEMA = new DoubleEMAIndicator(closePrice, 10);
        DoubleEMAIndicator longDEMA = new DoubleEMAIndicator(closePrice, 60);
        BollingerBandFacade bollinger = new BollingerBandFacade(series, 60, 3.5);


        SuperTrend st1_1Ind = new SuperTrend(series, 1, 1D);
        SuperTrend st10_1Ind = new SuperTrend(series, 10, 1D);
        SuperTrend st10_2Ind = new SuperTrend(series, 10, 2D);
        SuperTrend st5_3Ind = new SuperTrend(series, 5, 3D);
        SuperTrend st10_3Ind = new SuperTrend(series, 10, 3D);
        SuperTrend st10_4Ind = new SuperTrend(series, 10, 4D);
        SuperTrend st10_10Ind = new SuperTrend(series, 10, 10D);
        SuperTrend st15_4Ind = new SuperTrend(series, 15, 4D);
        SuperTrend st20_4Ind = new SuperTrend(series, 20, 4D);
        SuperTrend st20_5Ind = new SuperTrend(series, 20, 5D);

//        dataset.addSeries(buildChartBarSeries(series, bollinger.lower(), "B_L"));
        dataset.addSeries(buildChartBarSeries(series, shortDEMA, "ShortDEMA"));
        dataset.addSeries(buildChartBarSeries(series, longDEMA, "LongDEMA"));
//        dataset.addSeries(buildChartBarSeries(series, st1_1Ind, "st1_1Ind"));
        dataset.addSeries(buildChartBarSeries(series, st5_3Ind, "st5_3Ind"));
        dataset.addSeries(buildChartBarSeries(series, st10_3Ind, "st10_3Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st10_4Ind, "st10_4Ind"));
        dataset.addSeries(buildChartBarSeries(series, st20_4Ind, "st20_4Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st15_4Ind, "st15_4Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st20_5Ind, "st20_5Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st20_4Ind, "st20_4Ind"));
        dataset.addSeries(buildChartBarSeries(series, bollinger.upper(), "B_U"));


        CandlestickChart chart = new CandlestickChart();
//        chart.display(series, dataset, new DEMAStrategyDefinitionV3(symbol));
        chart.display(series, dataset, new DEMAStrategyDefinitionV6_1(symbol));
//        chart.display(series, dataset, new SuperTrendStrategyV1(symbol));
    }

}
