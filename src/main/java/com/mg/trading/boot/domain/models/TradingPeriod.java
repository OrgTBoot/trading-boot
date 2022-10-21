package com.mg.trading.boot.domain.models;

public enum TradingPeriod {
    EXTENDED("1"),
    REGULAR("0");

    public final String value;

    TradingPeriod(String value) {
        this.value = value;
    }
}
