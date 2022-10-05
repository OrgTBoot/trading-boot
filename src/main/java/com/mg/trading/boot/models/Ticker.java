package com.mg.trading.boot.models;

import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Ticker {
    private String externalId;
    private String symbol;
    private String company;
    private String industry;
    private String sector;
    private String country;
    private String marketCap;
    private BigDecimal peRatio;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal volume;
}
