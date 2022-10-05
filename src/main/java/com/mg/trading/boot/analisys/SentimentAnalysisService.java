package com.mg.trading.boot.analisys;

import com.mg.trading.boot.models.TickerNewsArticle;
import com.mg.trading.boot.models.npl.Sentiment;
import com.mg.trading.boot.models.npl.SentimentRecord;
import com.mg.trading.boot.models.npl.SentimentSummary;
import com.mg.trading.boot.models.npl.TickerSentiment;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class SentimentAnalysisService {
    private final StanfordCoreNLP instance;

    public SentimentAnalysisService() {
        String annotators = "tokenize, cleanxml, ssplit, parse, sentiment";
        this.instance = new StanfordCoreNLP(getProperties(annotators));
    }


    public TickerSentiment getSentimentByTickerArticles(List<TickerNewsArticle> articles) {
        List<SentimentSummary> summaries = articles.stream()
                .map(this::getArticleSentiment)
                .sorted(Comparator.comparing(SentimentSummary::getSentimentDate).reversed())
                .collect(Collectors.toList());

        int positive = (int) summaries.stream().filter(it -> Sentiment.POSITIVE.equals(it.getSentiment())).count();
        int negative = (int) summaries.stream().filter(it -> Sentiment.NEGATIVE.equals(it.getSentiment())).count();
        int neutral = (int) summaries.stream().filter(it -> Sentiment.NEUTRAL.equals(it.getSentiment())).count();

        Sentiment aggSentiment = calculateSentiment(positive, negative, neutral);

        return TickerSentiment.builder()
                .sentiment(aggSentiment)
                .positiveCount(positive)
                .negativeCount(negative)
                .neutralCount(neutral)
                .totalCount(summaries.size())
                .summaries(summaries)
                .build();
    }


    // -------------------Private Methods-----------------------------

    private SentimentSummary getArticleSentiment(TickerNewsArticle article) {
        String content = getArticleContent(article);
        List<SentimentRecord> articleSentiments = buildSentimentRecords(content);
        return getSentimentSummary(articleSentiments, article);
    }

    private SentimentSummary getSentimentSummary(List<SentimentRecord> records, TickerNewsArticle article) {
        List<SentimentRecord> positive = new ArrayList<>();
        List<SentimentRecord> negative = new ArrayList<>();
        List<SentimentRecord> neutral = new ArrayList<>();

        records.forEach(it -> {
            if (isPositive(it)) {
                positive.add(it);

            } else if (isNegative(it)) {
                negative.add(it);

            } else if (isNeutral(it)) {
                neutral.add(it);

            } else {
                log.warn("Could not classify sentiment: " + it);
            }
        });

        return SentimentSummary.builder()
                .sentiment(calculateSentiment(positive.size(), negative.size(), neutral.size()))
                .sentimentDate(article.getNewsTime())
                .content(getArticleContent(article))
                .build();

    }

    private Sentiment calculateSentiment(int positive, int negative, int neutral) {

        if (positive > negative && positive > neutral) {
            return Sentiment.POSITIVE;

        } else if (negative > positive && negative >= neutral) {
            return Sentiment.NEGATIVE;

        } else {
            return Sentiment.NEUTRAL;
        }
    }

    private List<SentimentRecord> buildSentimentRecords(String content) {
        Annotation annotation = instance.process(content);

        return annotation.get(CoreAnnotations.SentencesAnnotation.class).stream()
                .map(this::buildSentimentRecord)
                .collect(Collectors.toList());
    }

    private SentimentRecord buildSentimentRecord(CoreMap sentence) {
        Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        int sentimentInt = RNNCoreAnnotations.getPredictedClass(tree);
        String sentimentName = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        log.info(sentimentName + "\t" + sentimentInt + "\t" + sentence);

        return SentimentRecord.builder()
                .score(sentimentInt)
                .sentiment(sentimentName)
                .sentence(sentence.toString())
                .build();
    }

    private Properties getProperties(String annotators) {
        Properties properties = new Properties();
        properties.setProperty("annotators", annotators);

        return properties;
    }

    private boolean isNegative(SentimentRecord entry) {
        return entry.getScore() < 2;
    }

    private boolean isPositive(SentimentRecord entry) {
        return entry.getScore() > 2;
    }

    private boolean isNeutral(SentimentRecord entry) {
        return entry.getScore() == 2;
    }

    private String getArticleContent(TickerNewsArticle article) {
        List<String> contents = new ArrayList<>();
        Optional.ofNullable(article.getTitle()).ifPresent(contents::add);
        Optional.ofNullable(article.getContent()).ifPresent(contents::add);
        return String.join(". ", contents);
    }
}
