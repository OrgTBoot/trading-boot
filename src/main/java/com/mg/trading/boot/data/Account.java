package com.mg.trading.boot.data;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private List<Order> openOrders;
    private List<Position> positions;
}
