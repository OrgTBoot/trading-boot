package com.mg.trading.boot.logging;

public enum LogPackage {
    RULES("com.mg.trading.boot.domain.rules"),
    INDICATORS("com.mg.trading.boot.domain.rules.indicators");

    private final String value;

    LogPackage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
