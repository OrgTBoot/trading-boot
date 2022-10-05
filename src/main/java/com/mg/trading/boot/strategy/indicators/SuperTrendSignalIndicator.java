//package com.mg.trading.boot.strategy.indicators;
//
//import com.mg.trading.boot.models.OrderAction;
//import lombok.extern.log4j.Log4j2;
//import org.ta4j.core.BarSeries;
//import org.ta4j.core.indicators.CachedIndicator;
//
//@Log4j2
//public class SuperTrendSignalIndicator extends CachedIndicator<Boolean> {
//    private final Integer length;
//    private final OrderAction action;
//
//    public SuperTrendSignalIndicator(BarSeries series, Integer length, OrderAction action) {
//        super(series);
//        this.length = length;
//        this.action = action;
//    }
//
//    @Override
//    protected Boolean calculate(int index) {
//        final OrderAction signal = new SuperTrendIndicator(getBarSeries(), 3.0, this.length).getSignal(index);
//
//        boolean result = signal != null && signal.equals(this.action);
//        log.info("{} {} - {}", index, action, result);
//
//        return result;
//    }
//}
