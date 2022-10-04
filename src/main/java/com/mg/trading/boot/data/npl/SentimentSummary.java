package com.mg.trading.boot.data.npl;//package com.mg.trading.boot.data.npl;

import lombok.*;

import java.time.Instant;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SentimentSummary {
    private Sentiment sentiment;
    private Instant sentimentDate;
    private String content;
}
