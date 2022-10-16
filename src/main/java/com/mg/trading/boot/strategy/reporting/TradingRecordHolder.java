//package com.mg.trading.boot.strategy.reporting;
//
//import org.ta4j.core.Position;
//import org.ta4j.core.Trade;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.num.Num;
//
//import java.util.List;
//
//public class TradingRecordHolder implements TradingRecord {
//    @Override
//    public Trade.TradeType getStartingType() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return null;
//    }
//
//    @Override
//    public void operate(int index) {
//        TradingRecord.super.operate(index);
//    }
//
//    @Override
//    public void operate(int index, Num price, Num amount) {
//
//    }
//
//    @Override
//    public boolean enter(int index) {
//        return TradingRecord.super.enter(index);
//    }
//
//    @Override
//    public boolean enter(int index, Num price, Num amount) {
//        return false;
//    }
//
//    @Override
//    public boolean exit(int index) {
//        return TradingRecord.super.exit(index);
//    }
//
//    @Override
//    public boolean exit(int index, Num price, Num amount) {
//        return false;
//    }
//
//    @Override
//    public boolean isClosed() {
//        return TradingRecord.super.isClosed();
//    }
//
//    @Override
//    public List<Position> getPositions() {
//        return null;
//    }
//
//    @Override
//    public int getPositionCount() {
//        return TradingRecord.super.getPositionCount();
//    }
//
//    @Override
//    public Position getCurrentPosition() {
//        return null;
//    }
//
//    @Override
//    public Position getLastPosition() {
//        return TradingRecord.super.getLastPosition();
//    }
//
//    @Override
//    public Trade getLastTrade() {
//        return null;
//    }
//
//    @Override
//    public Trade getLastTrade(Trade.TradeType tradeType) {
//        return null;
//    }
//
//    @Override
//    public Trade getLastEntry() {
//        return null;
//    }
//
//    @Override
//    public Trade getLastExit() {
//        return null;
//    }
//}
