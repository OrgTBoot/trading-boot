package com.mg.trading.boot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.utils.BarSeriesUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.File;
import java.time.*;
import java.util.List;

public class TestDataProvider {

    @SneakyThrows
    public static List<TickerQuote> getQuotesFromFile(String fileName) {
        ClassLoader classLoader = TestDataProvider.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String data = FileUtils.readFileToString(file, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(data, new TypeReference<List<TickerQuote>>() {
        });
    }

    public static BarSeries getBarSeriesFromFile(String fileName) {
        List<TickerQuote> quotes = getQuotesFromFile(fileName);
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        return series;
    }

    public static Bar buildBar(int hour, int minute) {
        final ZoneId zoneId = BarSeriesUtils.getDefaultZone();
        final LocalDate date = LocalDate.of(2000, 1, 1);
        final LocalTime time = LocalTime.of(hour, minute);

        final ZonedDateTime dateTime = ZonedDateTime.of(date, time, zoneId);

        return new BaseBar(Duration.ZERO, dateTime, 0, 0, 0, 0, 0);
    }
}
