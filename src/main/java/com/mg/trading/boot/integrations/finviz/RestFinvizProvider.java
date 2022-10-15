package com.mg.trading.boot.integrations.finviz;

import com.mg.trading.boot.integrations.ScreenerProvider;
import com.mg.trading.boot.models.Ticker;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mg.trading.boot.utils.NumberUtils.toBigDecimal;

@Log4j2
@Service
@ConditionalOnProperty(value = "features.screener", havingValue = "finviz")
public class RestFinvizProvider implements ScreenerProvider {

    @Override
    @SneakyThrows
    public List<Ticker> getUnusualVolume() {
        final String url = FinvizEndpoints.UNUSUAL_VOLUME.value;

        Document doc = Jsoup.connect(url).get();
        Map<String, Integer> headers = getHeaders(doc);
        List<Map<String, String>> rows = getMappedRows(doc, headers);
        final List<Ticker> tickers = toTickers(rows);

        Comparator<Ticker> volumeComparator = Comparator.comparing(Ticker::getVolume, Comparator.reverseOrder());
        Comparator<Ticker> changeComparator = Comparator.comparing(Ticker::getChange, Comparator.reverseOrder());
        final List<Ticker> sortedTickers = tickers.stream()
                .sorted(volumeComparator.thenComparing(changeComparator))
                .collect(Collectors.toList());
        log.info("Screening {}: {}", FinvizEndpoints.UNUSUAL_VOLUME.name(), url);
        log.info("\n" + buildTable(sortedTickers).render());

        return sortedTickers;
    }

    //----------------------------------------------------------
    //-------------Private Methods------------------------------
    //----------------------------------------------------------

    private Map<String, Integer> getHeaders(Document doc) {
        Elements headers = doc.select("tr[valign=middle] td");

        Map<String, Integer> mapOfHeaders = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            mapOfHeaders.put(headers.get(i).text(), i);
        }

        return mapOfHeaders;
    }

    private List<Map<String, String>> getMappedRows(Document doc, Map<String, Integer> headers) {
        List<Map<String, String>> mapOfTickers = new ArrayList<>();

        Elements tableRows = doc.select("tr[valign=top]");
        for (Element row : tableRows) {
            Elements cells = row.select("td[class=screener-body-table-nw]");
            Map<String, String> ticker = new HashMap<>();

            for (Map.Entry<String, Integer> header : headers.entrySet()) {
                ticker.put(header.getKey(), cells.get(header.getValue()).text());
            }
            mapOfTickers.add(ticker);
        }

        return mapOfTickers;
    }

    private AsciiTable buildTable(List<Ticker> tickers) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("NR.", "SYMBOL", "COMPANY", "INDUSTRY", "MARKET CAP", "P/E", "PRICE", "CHANGE %", "VOLUME").setTextAlignment(TextAlignment.CENTER);
        table.addRule();

        tickers.forEach(it -> {
            AT_Row row = table.addRow(
                    it.getExternalId(), it.getSymbol(), it.getCompany(), it.getIndustry(), it.getMarketCap(),
                    it.getPeRatio(), it.getPrice(), it.getChange() + "%", it.getVolume());
            //align numbers to the left
            IntStream.of(4, 5, 6, 7).forEach(colIdx -> row.getCells().get(colIdx).getContext().setTextAlignment(TextAlignment.RIGHT));
            table.addRule();
        });

        table.getRenderer().setCWC(new CWC_LongestLine());
        return table;
    }


    private static List<Ticker> toTickers(List<Map<String, String>> listOfFinVizMaps) {
        return listOfFinVizMaps.stream().map(it ->
                        Ticker.builder()
                                .externalId(it.get("No."))
                                .symbol(it.get("Ticker"))
                                .company(it.get("Company"))
                                .industry(it.get("Industry"))
                                .sector(it.get("Sector"))
                                .country(it.get("Country"))
                                .marketCap(it.get("Market Cap"))
                                .peRatio(toBigDecimal(it.get("P/E")))
                                .price(toBigDecimal(it.get("Price")))
                                .change(toBigDecimal(it.get("Change")))
                                .volume(toBigDecimal(it.get("Volume")))
                                .build())
                .collect(Collectors.toList());
    }

}
