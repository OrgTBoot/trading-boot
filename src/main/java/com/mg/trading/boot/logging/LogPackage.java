package com.mg.trading.boot.logging;

import java.util.Arrays;
import java.util.List;

public enum LogPackage {
    APP(Arrays.asList("com.mg.trading.boot")),
    RULES(Arrays.asList("com.mg.trading.boot.domain.rules")),
    INDICATORS(Arrays.asList(
            "org.ta4j.core.indicators",
            "com.mg.trading.boot.domain.indicators"));

    private final List<String> packages;

    LogPackage(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getPackages() {
        return packages;
    }
}
