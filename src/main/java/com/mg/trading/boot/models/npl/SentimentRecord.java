package com.mg.trading.boot.models.npl;

import lombok.*;


@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SentimentRecord {
    private int score;
    private String sentiment;
    private String sentence;
}
