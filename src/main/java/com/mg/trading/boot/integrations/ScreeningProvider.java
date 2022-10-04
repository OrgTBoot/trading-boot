package com.mg.trading.boot.integrations;

import com.mg.trading.boot.data.Ticker;

import java.util.List;

public interface ScreeningProvider {

    List<Ticker> getTopGaines();

    List<Ticker> getUnusualVolume();

    List<Ticker> getTopLosers();
}
