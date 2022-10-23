package com.mg.trading.boot.domain.strategy.dema2;

import com.mg.trading.boot.domain.models.TickerQuote;
import com.mg.trading.boot.tbd.TestDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mg.trading.boot.tbd.TestDataProvider.buildQuote;

public class DEMAStrategyDefinitionV2Test {

    @Test
    public void entryRuleTest() {
        List<TickerQuote> quoteList = new ArrayList<>();
        quoteList.add(buildQuote(10, 0, BigDecimal.ONE));
        quoteList.add(buildQuote(10, 1, BigDecimal.ONE));
        quoteList.add(buildQuote(10, 2, BigDecimal.ONE));

        TradingRecord tradingRecord = new BaseTradingRecord();

        DEMAStrategyDefinitionV2 strategyDef = new DEMAStrategyDefinitionV2("DUMMY");
        strategyDef.updateSeries(quoteList);

        Strategy strategy = strategyDef.getStrategy();
        Rule entryRule = strategy.getEntryRule();

        int endIndex = strategyDef.getSeries().getEndIndex();
        Assert.assertFalse(entryRule.isSatisfied(endIndex, tradingRecord));

    }
}
