package com.mg.trading.boot.tbd;

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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestDataProvider {

    @SneakyThrows
    public static List<File> getQuoteFiles() {
        return getQuoteFiles("./src/test/resources");
    }

    @SneakyThrows
    public static List<File> getQuoteFiles(String path) {
        List<File> files = Files.find(Paths.get(path), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
                .map(Path::toFile).collect(Collectors.toList());

        return files.stream().filter(it -> it.getName().toLowerCase().endsWith("json")).collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<TickerQuote> getQuotesFromFile(File file) {
        String data = FileUtils.readFileToString(file, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(data, new TypeReference<List<TickerQuote>>() {
        });
    }

    @SneakyThrows
    public static List<TickerQuote> getQuotesFromFile(String fileName) {
        ClassLoader classLoader = TestDataProvider.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return getQuotesFromFile(file);
    }

    public static BarSeries getBarSeriesFromFile(String fileName) {
        List<TickerQuote> quotes = getQuotesFromFile(fileName);
        BarSeries series = new BaseBarSeries();
        BarSeriesUtils.addBarSeries(series, quotes, Duration.ofSeconds(60));

        return series;
    }


    public static BarSeries buildBarSeries() {
        return new BaseBarSeries();
    }

    public static Bar buildBar(int hour, int minute) {
        return buildBar(hour, minute, BigDecimal.ONE);
    }

    public static Bar buildBar(int hour, int minute, BigDecimal price) {
        final ZoneId zoneId = BarSeriesUtils.getDefaultZone();
        final LocalDate date = LocalDate.of(2000, 1, 1);
        final LocalTime time = LocalTime.of(hour, minute);

        final ZonedDateTime dateTime = ZonedDateTime.of(date, time, zoneId);

        return new BaseBar(Duration.ZERO, dateTime, price, price, price, price, BigDecimal.ONE);
    }

    public static TickerQuote buildQuote(int hour, int minute, BigDecimal price) {
        final ZoneId zoneId = BarSeriesUtils.getDefaultZone();
        final LocalDate date = LocalDate.of(2000, 1, 1);
        final LocalTime time = LocalTime.of(hour, minute);
        final ZonedDateTime dateTime = ZonedDateTime.of(date, time, zoneId);

        return TickerQuote.builder()
                .timeZone(zoneId.toString())
                .timeStamp(dateTime.toInstant().toEpochMilli())
                .openPrice(price)
                .closePrice(price)
                .lowPrice(price)
                .highPrice(price)
                .volume(1000L)
                .build();
    }
}
