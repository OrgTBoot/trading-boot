package com.mg.trading.boot.data;

import lombok.*;

import java.time.Instant;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TickerNewsArticle {
    private String externalId;
    private String title;
    private String newsUrl;
    private String sourceName;
    private String content;
    private Instant newsTime;
    private long newsDaysAgo;
}
