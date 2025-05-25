package org.example.url_shorter_test_task_gorokhov.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Getter
@Component
public class AppConfig {

    @Value("${app.ttl-minutes:10}")
    private long ttlMinutes;

    @Value("${app.attempts:5}")
    private int maxAttempts;

    @Value("${app.protocol:https}")
    private String protocol;

    @Value("${app.host:localhost}")
    private String host;

    @Value("#{'${app.allowed-ports:8080,8081,8082}'.split(',')}")
    private List<String> allowedPorts;

    @Value("${app.path:api}")
    private String path;

    public Duration getTtl() {
        return Duration.ofMinutes(ttlMinutes);
    }

}
