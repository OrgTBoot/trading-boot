package com.mg.trading.boot.integrations;

import com.mg.trading.boot.models.Ticker;

import java.util.List;

public interface ScreenerProvider {

    List<Ticker> getUnusualVolume();

}
