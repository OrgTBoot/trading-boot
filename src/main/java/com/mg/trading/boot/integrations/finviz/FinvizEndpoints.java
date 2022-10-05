package com.mg.trading.boot.integrations.finviz;

public enum FinvizEndpoints {
    UNUSUAL_VOLUME("https://finviz.com/screener.ashx?v=111&s=ta_unusualvolume&f=cap_smallover,geo_usa,ind_stocksonly,sh_avgvol_o400,sh_price_u10,ta_perf_dup,ta_volatility_wo4&ft=4&ta=0&o=-volume"),
    /**
     * See <a href="https://www.youtube.com/watch?v=7xKOo6vNaq8"></a>
     */
    SMALL_PLUS_CAP_TOP_GAINERS("https://finviz.com/screener.ashx?v=111&s=ta_topgainers&f=cap_smallover,geo_usa,ind_stocksonly,sh_avgvol_o200,sh_price_o5,ta_perf_dup,ta_sma20_pa,ta_sma200_pa,ta_sma50_pa&ft=4&ta=0&o=-volume");


    public final String value;

    private FinvizEndpoints(String value) {
        this.value = value;
    }
}
