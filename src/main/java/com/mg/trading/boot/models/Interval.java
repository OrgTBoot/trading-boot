package com.mg.trading.boot.models;

public enum Interval {
    ONE_MINUTE("1", "m", 60L),
    FIVE_MINUTES("5", "m", 60 * 5L),
    FIFTEEN_MINUTES("15", "m", 60 * 15L),
    THIRTY_MINUTES("30", "m", 60 * 30L),
    ONE_HOUR("60", "m", 60 * 60L),
    TOW_HOURS("120", "m", 60 * 120L),
    FOUR_HOURS("240", "m", 60 * 240L),
    DAY("1", "d", 60 * 60 * 12L);

    public final String value;
    public final String unit;
    public final Long seconds;

    Interval(String value, String unit, Long seconds) {
        this.value = value;
        this.unit = unit;
        this.seconds = seconds;
    }
}
