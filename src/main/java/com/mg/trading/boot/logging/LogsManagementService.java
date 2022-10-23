package com.mg.trading.boot.logging;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static com.mg.trading.boot.config.BeanConfig.SERVICE_REST_TEMPLATE;

@Component
public class LogsManagementService extends AbstractRestProvider {
    private int serverPort;

    public LogsManagementService(@Value("${server.port}") final int serverPort, @Qualifier(SERVICE_REST_TEMPLATE) final RestTemplate restTemplate) {
        super(restTemplate);
        this.serverPort = serverPort;
    }

    public void updateLogLevel(LogPackage logPackage, LogLevel level) {
        logPackage.getPackages().forEach(pkg -> {
            String url = String.format("http://localhost:%s/actuator/loggers/%s?configuredLevel=%s", serverPort, pkg, level.name());
            post(url, null, new ParameterizedTypeReference<String>() {
            });
        });
    }
}
