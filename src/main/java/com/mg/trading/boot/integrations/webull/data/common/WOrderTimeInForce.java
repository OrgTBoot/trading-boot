package com.mg.trading.boot.integrations.webull.data.common;

/**
 * A day order is an order to buy or sell a stock that automatically expires if not executed on the day the order was
 * placed. They do not automatically carry over into after-hours trading or the next regular trading day.
 */
public enum WOrderTimeInForce {
    DAY,
    GTC
}
