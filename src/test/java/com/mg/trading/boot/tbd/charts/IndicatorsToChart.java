package com.mg.trading.boot.tbd.charts;

import com.mg.trading.boot.domain.indicators.supertrentv2.SuperTrend;
import com.mg.trading.boot.tbd.TestDataProvider;
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
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.ZLEMAIndicator;
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
        ApplicationFrame frame = new ApplicationFrame("Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        String fileName = "tmp/ARDX.json_exc";
        BarSeries series = TestDataProvider.getBarSeriesFromFile(fileName);
//        SuperTrend superTrend1 = new SuperTrend(series, 10,3D);
//        SuperTrend superTrend2 = new SuperTrend(series, 20,6D);
        SuperTrend superTrend1 = new SuperTrend(series, 60,3D);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        DoubleEMAIndicator dema60 = new DoubleEMAIndicator(closePrice, 60);
        DoubleEMAIndicator dema10 = new DoubleEMAIndicator(closePrice, 60);
        DoubleEMAIndicator shortIndicator = new DoubleEMAIndicator(closePrice, 10);

//        ZLEMAIndicator zlema10 = new ZLEMAIndicator(dema60, 10);
        ZLEMAIndicator zlema60 = new ZLEMAIndicator(closePrice, 60);
//        ChandelierExitLongIndicator chandelierLong = new ChandelierExitLongIndicator(series, 3, 3);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartBarSeries(series, closePrice, fileName));
        dataset.addSeries(buildChartBarSeries(series, superTrend1, "ST1"));
//        dataset.addSeries(buildChartBarSeries(series, superTrend2, "ST2"));
//        dataset.addSeries(buildChartBarSeries(series, longIndicator, "DEMA 60"));
//        dataset.addSeries(buildChartBarSeries(series, zlema10, "ZELMA10"));
        dataset.addSeries(buildChartBarSeries(series, zlema60, "ZELMA60"));
//        dataset.addSeries(buildChartBarSeries(series, chandelierLong, "CHAND"));

        JFreeChart chart = ChartFactory.createTimeSeriesChart(fileName, // title
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

        displayChart(chart);
    }
}
