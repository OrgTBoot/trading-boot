package com.mg.trading.boot.domain.models;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TradingLog {
    private String symbol;
    private Integer daysRange;
    private List<Order> filledOrders;
    private List<Order> openOrders;
    private List<Position> positions;
}
