package com.mg.trading.boot.models;

public enum Interval {
    ONE_MINUTE("m1", 60L),
    FIFE_MINUTES("m5", 60 * 5L),
    FIFTEEN_MINUTES("m15", 60 * 15L),
    THIRTY_MINUTES("m30", 60 * 30L),
    ONE_HOUR("m60", 60 * 60L),
    TOW_HOURS("m120", 60 * 120L),
    FOUR_HOURS("m240", 60 * 240L),
    DAY("d1", 60 * 60 * 12L);

    public final String value;
    public final Long seconds;

    Interval(String value, Long seconds) {
        this.value = value;
        this.seconds = seconds;
    }
}
