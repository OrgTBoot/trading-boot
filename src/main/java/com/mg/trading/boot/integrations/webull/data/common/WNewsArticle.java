package com.mg.trading.boot.integrations.webull.data.common;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WNewsArticle {
    private long id;
    private String title;
    private String sourceName;
    private String summary;
    private String newsUrl;
    private String siteType;
    private String collectSource;
    private Instant newsTime;
}
