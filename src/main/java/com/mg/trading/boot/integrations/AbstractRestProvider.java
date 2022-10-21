package com.mg.trading.boot.integrations;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Log4j2
public abstract class AbstractRestProvider {
    protected RestTemplate restTemplate;

    public AbstractRestProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<?> get(String url, ParameterizedTypeReference<?> typeReference) {
        return exchange(url, HttpMethod.GET, null, typeReference);
    }

    protected ResponseEntity<?> post(String url, Object body, ParameterizedTypeReference<?> typeReference) {
        return exchange(url, HttpMethod.POST, body, typeReference);
    }

    private ResponseEntity<?> exchange(String url, HttpMethod method, Object body, ParameterizedTypeReference<?> typeReference) {
        ResponseEntity<?> response;
        try {
            RequestEntity<?> request = new RequestEntity<>(body, method, URI.create(url));
            response = this.restTemplate.exchange(request, typeReference);

        } catch (Exception e) {
            log.error("Remote service error: {}", e.getCause(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        List<HttpStatus> okCodes = Arrays.asList(HttpStatus.OK, HttpStatus.NO_CONTENT);
        if (!okCodes.contains(response.getStatusCode())) {
            log.error("Remote service error, unexpected status code: {}", response.getStatusCode().value());
            throw new RuntimeException("Unexpected status code on order placement: " + response.getStatusCode());
        }

        return response;
    }

}
