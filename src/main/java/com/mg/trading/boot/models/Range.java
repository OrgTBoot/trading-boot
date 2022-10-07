package com.mg.trading.boot.models;

public enum Range {
    FIFE_MINUTES(5, "m", 5 * 60L),
    TEN_MINUTES(10, "m", 10 * 60L),
    ONE_DAY(1, "d", 12 * 60 * 60L),
    FIFE_DAYS(5, "d", 5 * 12 * 60 * 60L);

    public final Integer value;
    public final String unit;
    public final Long seconds;

    Range(Integer value, String unit, Long seconds) {
        this.value = value;
        this.unit = unit;
        this.seconds = seconds;
    }
}
