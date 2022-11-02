package com.mg.trading.boot.integrations.webull;

import com.mg.trading.boot.integrations.AbstractRestProvider;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.mg.trading.boot.config.BeanConfig.SERVICE_REST_TEMPLATE;

@Log4j2
@Component
public class WbAuthProvider extends AbstractRestProvider {

    private static String tradePinKey;
    private static String tradePinToken;
    private static String authKey;
    private static String authSecret;
    private static String did;

    public WbAuthProvider(@Qualifier(SERVICE_REST_TEMPLATE) final RestTemplate restTemplate,
                          @Value("${features.paper-trading}") final Boolean paperTrading,
                          @Value("${providers.webull.auth.key}") final String authenticationKey,
                          @Value("${providers.webull.auth.secret}") final String authenticationSecret,
                          @Value("${providers.webull.trade-account.pin.key}") String pinKey,
                          @Value("${providers.webull.trade-account.pin.secret}") final String pinValue) {
        super(restTemplate);
        authKey = authenticationKey;
        authSecret = authenticationSecret;
        tradePinKey = pinKey;
        did = UUID.randomUUID().toString();

        if (!paperTrading) {
            ScheduledExecutorService executorService = getScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> refreshToken(pinValue), 0, 60, TimeUnit.MINUTES);
            log.info("Trading PIN token refresher is scheduled.");
        }

    }


    public static String getAuthKey() {
        return authKey;
    }

    public static String getAuthSecret() {
        return authSecret;
    }

    public static String getPinKey() {
        return tradePinKey;
    }

    public static String getPinToken() {
        return tradePinToken;
    }

    public static String getDid() {
        return did;
    }

    @SneakyThrows
    private void refreshToken(String pin) {
        String url = WUrls.tradingToken();

        Map<String, String> body = new HashMap<>();
        body.put("pwd", getEncodedPin(pin));

        HttpHeaders headers = new HttpHeaders();
        headers.add(authKey, authSecret);
        headers.add("did", did);

        Map<String, String> responseEntityBody = (Map<String, String>) post(url, body, headers, new ParameterizedTypeReference<Map<String, String>>() {
        }).getBody();

        tradePinToken = responseEntityBody.get("tradeToken");
        log.info("Trading PIN retrieved: {} wth DID {}", tradePinToken, did);
    }

    private String getEncodedPin(String pin) throws NoSuchAlgorithmException {
        String rawPin = "wl_app-a&b@!423^" + pin;

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(rawPin.getBytes(StandardCharsets.UTF_8));
        byte[] result = md5.digest();

        return DatatypeConverter.printHexBinary(result).toLowerCase();
    }

    private ScheduledExecutorService getScheduledExecutor() {
        String name = this.getClass().getSimpleName();
        ThreadFactory threadFactory = runnable -> new Thread(runnable, name);

        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}
