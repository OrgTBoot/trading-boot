package com.mg.trading.boot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.models.TickerQuote;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class TestDataProvider {

    @SneakyThrows
    public static List<TickerQuote> getQuotesFromFile(String fileName) {
        ClassLoader classLoader = TestDataProvider.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String data = FileUtils.readFileToString(file, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
//
        return objectMapper.readValue(data, new TypeReference<List<TickerQuote>>() {
        });
    }

    public static BarSeries getBarSeriesFromFile(String fileName) {
        List<TickerQuote> quotes = getQuotesFromFile(fileName);
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        return series;
    }
}
