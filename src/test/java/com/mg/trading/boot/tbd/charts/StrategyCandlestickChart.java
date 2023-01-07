package com.mg.trading.boot.tbd.charts;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.domain.strategy.dema7.DEMAStrategyDefinitionV7;
import com.mg.trading.boot.domain.strategy.etf2.ETFStrategyV2;
import com.mg.trading.boot.domain.strategy.etf3.ETFStrategyV3;
import com.mg.trading.boot.tbd.TestDataProvider;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.*;
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
        String fileName = "ETF/12_30_2022_ETF/4_SOXS.json";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        DoubleEMAIndicator DEMA_60 = new DoubleEMAIndicator(closePrice, 60);
//        DoubleEMAIndicator DEMA_10 = new DoubleEMAIndicator(closePrice, 10);
//
//        ZLEMAIndicator zlemaIndicator = new ZLEMAIndicator(closePrice, 60);
//        BollingerBandFacade bollinger = new BollingerBandFacade(series, 60, 3.5);
//        CCIIndicator shortCci = new CCIIndicator(series, 5);
//
//
//        SuperTrend st10_1Ind = new SuperTrend(series, 10, 1D);
//        SuperTrend st11_2Ind = new SuperTrend(series, 11, 2D);
//        SuperTrend st10_3Ind = new SuperTrend(series, 10, 3D);
//        SuperTrend st13_3Ind = new SuperTrend(series, 13, 3D);

        EMAIndicator EMA_9 = new EMAIndicator(closePrice, 9);
        EMAIndicator EMA_26 = new EMAIndicator(closePrice, 26);
//        MACDIndicator MACD_9_26 = new MACDIndicator(closePrice, 9, 26);
//        EMAIndicator EMA_MACD_18 = new EMAIndicator(MACD_9_26, 18);

//        StochasticOscillatorKIndicator STOCHASTIC_14 = new StochasticOscillatorKIndicator(series, 14);

        dataset.addSeries(buildChartBarSeries(series, EMA_9, "EMA_9"));
        dataset.addSeries(buildChartBarSeries(series, EMA_26, "EMA_26"));
//        dataset.addSeries(buildChartBarSeries(series, MACD_9_26, "MACD_9_26"));
//        dataset.addSeries(buildChartBarSeries(series, EMA_MACD_18, "EMA_MACD_18"));
//        dataset.addSeries(buildChartBarSeries(series, STOCHASTIC_14, "STOCHASTIC_14"));
//        dataset.addSeries(buildChartBarSeries(series, st11_2Ind, "st11_2Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st10_3Ind, "st10_3Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st13_3Ind, "st13_3Ind"));

//        dataset.addSeries(buildChartBarSeries(series, DEMA_60, "DEMA_60"));
//        dataset.addSeries(buildChartBarSeries(series, DEMA_10, "DEMA_10"));

//        dataset.addSeries(buildChartBarSeries(series, st60_3Ind, "st60_3Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st15_4Ind, "st15_4Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st20_5Ind, "st20_5Ind"));
//        dataset.addSeries(buildChartBarSeries(series, st20_4Ind, "st20_4Ind"));
//        dataset.addSeries(buildChartBarSeries(series, bollinger.upper(), "B_U"));
//        dataset.addSeries(buildChartBarSeries(series, shortCci, "cci"));
//        dataset.addSeries(buildChartBarSeries(series, zlemaIndicator, "ZLEMA60"));


        CandlestickChart chart = new CandlestickChart();
//        chart.display(series, dataset, new DEMAStrategyDefinitionV3(symbol));
//        chart.display(series, dataset, new DEMAStrategyDefinitionV6_1(symbol));
//        chart.display(series, dataset, new ETFStrategyV2(symbol));
//        chart.display(series, dataset, new DEMAStrategyDefinitionV7(symbol));
        chart.display(series, dataset, new ETFStrategyV3(symbol));
    }

}
