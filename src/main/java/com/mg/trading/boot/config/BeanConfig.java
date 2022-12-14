package com.mg.trading.boot.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import static com.mg.trading.boot.integrations.webull.WbAuthProvider.*;

@Configuration
public class BeanConfig {
    public final static String WEBULL_REST_TEMPLATE = "webull_rest_template";
    public final static String YAHOO_REST_TEMPLATE = "yahoo_rest_template";
    public final static String SERVICE_REST_TEMPLATE = "service_rest_template";

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
        return builder.build();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    @Qualifier(SERVICE_REST_TEMPLATE)
    public RestTemplate getServiceRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, clientHttpRequestExecution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.add("Content-Type", "application/json");

            return clientHttpRequestExecution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    @Qualifier(YAHOO_REST_TEMPLATE)
    public RestTemplate getYahooRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Qualifier(WEBULL_REST_TEMPLATE)
    public RestTemplate getWbRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, clientHttpRequestExecution) -> {

            HttpHeaders headers = request.getHeaders();
            headers.add(getAuthKey(), getAuthSecret());
            headers.add(getPinKey(), getPinToken());
            headers.add("did", getDid()); //needs to be same as the one used to get pinToken

            headers.add("app", "global");
            headers.add("app-group", "broker");
            headers.add("appid", "wb_web_app");
            headers.add("device-type", "Web");
            headers.add("ver", "3.39.23");
            headers.add("platform", "web");
            headers.add("tz", "America/Los_Angeles");

            return clientHttpRequestExecution.execute(request, body);
        });
        return restTemplate;
    }
}
