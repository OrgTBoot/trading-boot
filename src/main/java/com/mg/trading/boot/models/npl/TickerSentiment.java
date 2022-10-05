package com.mg.trading.boot.models.npl;

import lombok.*;

import java.util.List;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TickerSentiment {
    private Sentiment sentiment;
    private int totalCount;
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private List<SentimentSummary> summaries;
}
