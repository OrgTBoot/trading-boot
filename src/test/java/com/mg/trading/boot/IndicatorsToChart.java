package com.mg.trading.boot;

import com.mg.trading.boot.strategy.indicators.SuperTrendBuyIndicator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ChandelierExitShortIndicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IndicatorsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j bar series and an indicator.
     *
     * @param barSeries the ta4j bar series
     * @param indicator the indicator
     * @param name      the name of the chart time series
     * @return the JFreeChart time series
     */
    private static TimeSeries buildChartBarSeries(BarSeries barSeries, Indicator<Num> indicator, String name) {
        TimeSeries chartTimeSeries = new TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            Minute minute = new Minute(Date.from(bar.getEndTime().toInstant()));
            chartTimeSeries.add(minute, indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        /*
         * Getting bar series
         */
        String fileName = "IMVT_10_08_2022.json";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);

        /*
         * Creating indicators
         */
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator longDEMA = new DoubleEMAIndicator(closePrice, 60);
        DoubleEMAIndicator shortDEMA = new DoubleEMAIndicator(closePrice, 10);
        SuperTrendBuyIndicator buyTrend = new SuperTrendBuyIndicator(series, 10);

        ChandelierExitShortIndicator shortChandelierExit = new ChandelierExitShortIndicator(series, 5, 3);
//        ChandelierExitLongIndicator longChandelierExit = new ChandelierExitLongIndicator(series, 5, 3);

        // Bollinger bands
//        SMAIndicator smaIndicator = new SMAIndicator(closePrice, 15);
//        StandardDeviationIndicator sd14 = new StandardDeviationIndicator(closePrice, 20);
//
//        BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(smaIndicator);
//        BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd14);
//        BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd14);

        /*
         * Building chart dataset
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartBarSeries(series, closePrice, fileName));
        dataset.addSeries(buildChartBarSeries(series, longDEMA, "Long DEMA"));
        dataset.addSeries(buildChartBarSeries(series, shortDEMA, "Short DEMA"));
        dataset.addSeries(buildChartBarSeries(series, shortChandelierExit, "ShortChandelierExit"));
//        dataset.addSeries(buildChartBarSeries(series, buyTrend, "BuyTrend"));
//        dataset.addSeries(buildChartBarSeries(series, shortChandelierExit, "Chandelier Short"));
//        dataset.addSeries(buildChartBarSeries(series, longChandelierExit, "Chandelier Long"));
//        dataset.addSeries(buildChartBarSeries(series, lowBBand, "Low Bollinger Band"));
//        dataset.addSeries(buildChartBarSeries(series, middleBBand, "Middle Bollinger Band"));
//        dataset.addSeries(buildChartBarSeries(series, upBBand, "High Bollinger Band"));

        /*
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart("IMVT_10_08_2022.json", // title
                "Date", // x-axis label
                "Price Per Unit", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        /*
         * Displaying the chart
         */
        displayChart(chart);
    }
}
