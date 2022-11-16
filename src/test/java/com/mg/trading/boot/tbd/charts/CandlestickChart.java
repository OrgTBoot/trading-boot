/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2022 Ta4j Organization & respective
 * authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.mg.trading.boot.tbd.charts;

import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.tbd.TestDataProvider;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import ta4jexamples.loaders.CsvTradesLoader;

import java.awt.*;
import java.util.Date;

/**
 * This class builds a traditional candlestick chart.
 */
public class CandlestickChart {

    /**
     * Builds a JFreeChart OHLC dataset from a ta4j bar series.
     *
     * @param series the bar series
     * @return an Open-High-Low-Close dataset
     */
    private static OHLCDataset createOHLCDataset(BarSeries series) {
        final int nbBars = series.getBarCount();

        Date[] dates = new Date[nbBars];
        double[] opens = new double[nbBars];
        double[] highs = new double[nbBars];
        double[] lows = new double[nbBars];
        double[] closes = new double[nbBars];
        double[] volumes = new double[nbBars];

        for (int i = 0; i < nbBars; i++) {
            Bar bar = series.getBar(i);
            dates[i] = new Date(bar.getEndTime().toEpochSecond() * 1000);
            opens[i] = bar.getOpenPrice().doubleValue();
            highs[i] = bar.getHighPrice().doubleValue();
            lows[i] = bar.getLowPrice().doubleValue();
            closes[i] = bar.getClosePrice().doubleValue();
            volumes[i] = bar.getVolume().doubleValue();
        }

        return new DefaultHighLowDataset("stock", dates, highs, lows, opens, closes, volumes);
    }

    /**
     * Builds an additional JFreeChart dataset from a ta4j bar series.
     *
     * @param series the bar series
     * @return an additional dataset
     */
//    private static TimeSeriesCollection createAdditionalDataset(BarSeries series) {
//        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
//        TimeSeriesCollection dataset = new TimeSeriesCollection();
//        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries("Btc price");
//        for (int i = 0; i < series.getBarCount(); i++) {
//            Bar bar = series.getBar(i);
//            chartTimeSeries.add(new Second(new Date(bar.getEndTime().toEpochSecond() * 1000)),
//                    indicator.getValue(i).doubleValue());
//        }
//        dataset.addSeries(chartTimeSeries);
//        return dataset;
//    }

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
        panel.setPreferredSize(new java.awt.Dimension(740, 300));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Candlestick chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        String fileName = "11_07_2022/SQQQ.json";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator longIndicator = new DoubleEMAIndicator(closePrice, 60);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, 10);
        SuperTrend superTrend = new SuperTrend(series, 10,3D);

        OHLCDataset ohlcDataset = createOHLCDataset(series);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartBarSeries(series, closePrice, "Stock"));
        dataset.addSeries(buildChartBarSeries(series, superTrend, "SuperTrend"));
        dataset.addSeries(buildChartBarSeries(series, longIndicator, "DEMA 60"));
        dataset.addSeries(buildChartBarSeries(series, shortIndicator, "DEMA 10"));

        JFreeChart chart = ChartFactory.createCandlestickChart("V5", "Time", "USD", ohlcDataset, true);

        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(renderer);
        // Additional dataset
        int index = 1;
        plot.setDataset(index, dataset);
        plot.mapDatasetToRangeAxis(index, 0);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
        renderer2.setSeriesPaint(index, Color.blue);
        plot.setRenderer(index, renderer2);
        // Misc
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setBackgroundPaint(Color.white);
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        /*
         * Displaying the chart
         */
        displayChart(chart);
    }
}
