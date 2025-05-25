package org.example.url_shorter_test_task_gorokhov.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.url_shorter_test_task_gorokhov.config.AppConfig;
import org.example.url_shorter_test_task_gorokhov.exception.LinkExpiredException;
import org.example.url_shorter_test_task_gorokhov.exception.ParseShortCodeException;
import org.example.url_shorter_test_task_gorokhov.exception.ShortCodeGenerationException;
import org.example.url_shorter_test_task_gorokhov.limit.RateLimiter;
import org.example.url_shorter_test_task_gorokhov.repository.UrlMapping;
import org.example.url_shorter_test_task_gorokhov.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class UrlService {

    private final AppConfig config;
    private final UrlRepository repository;
    private final RateLimiter rateLimiter;

    public UrlService(AppConfig config,
                      UrlRepository repository,
                      /**
                       * Поменяй реализацию RateLimiter:
                       * distributedUserRateLimiterService - распределенное хранилище семафоров (гарантирует не более 100 запросов на всех инстансах)
                       * userRateLimiterService - гарантирует не более 100 запросов в рамках одного инстанса (локальная реализация)
                       */
                      @Qualifier("distributedUserRateLimiterService") RateLimiter rateLimiter) {
        this.config = config;
        this.repository = repository;
        this.rateLimiter = rateLimiter;
    }

    public String shorten(String fullUrl) {
        log.debug("Attempting to shorten URL: {}", fullUrl);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        String permitId = rateLimiter.tryAcquire(userId);

        try {
            /**
             * Раскомментируй это, чтобы выполнялось ровно 100 запросов для пользователя
             */
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String prefix = getUrlPrefix();
            Optional<UrlMapping> existing = repository.findByFullUrl(fullUrl);
            if (existing.isPresent()) {
                log.info("Found existing mapping for URL: {}", fullUrl);
                return concatinateString(prefix, existing.get().getShortCode());
            }
            for (int attempt = 0; attempt < config.getMaxAttempts(); attempt++) {
                String candidateCode = generateCode(fullUrl, attempt);
                Optional<UrlMapping> byCode = repository.findById(candidateCode);

                if (byCode.isEmpty()) {
                    log.info("Generated new short code: {} for URL: {}", candidateCode, fullUrl);
                    UrlMapping mapping = new UrlMapping();
                    mapping.setShortCode(candidateCode);
                    mapping.setFullUrl(fullUrl);
                    mapping.setUpdatedAt(Instant.now());
                    repository.save(mapping);
                    return concatinateString(prefix, candidateCode);
                } else if (byCode.get().getFullUrl().equals(fullUrl)) {
                    log.info("Code collision resolved with same URL: {}", fullUrl);
                    return concatinateString(prefix, candidateCode);
                } else {
                    log.warn("Code collision detected for code: {}", candidateCode);
                }
            }
            log.error("Failed to generate unique short code after {} attempts", config.getMaxAttempts());
            throw new ShortCodeGenerationException("Failed to generate unique short code after " + config.getMaxAttempts() + " attempts");
        }
        finally {
            rateLimiter.release(userId, permitId);
        }
    }

    public void prolongShortUrl(String shortUrl) {
        log.debug("Prolonging short URL: {}", shortUrl);
        String shortCode = extractShortCode(shortUrl);
        UrlMapping mapping = repository.findById(shortCode)
                .orElseThrow(() -> {
                    log.error("Short link not found: {}", shortCode);
                    return new EntityNotFoundException("Short link not found: " + shortCode);
                });
        mapping.setUpdatedAt(Instant.now());
        repository.save(mapping);
        log.info("Updated timestamp for short code: {}", shortCode);
    }

    public String expandByShortUrl(String shortUrl) {
        log.debug("Expanding short URL: {}", shortUrl);
        String shortCode = extractShortCode(shortUrl);
        UrlMapping mapping = getUrlMapping(shortCode);
        return mapping.getFullUrl();
    }

    public String expandForRedirect(String shortCode) {
        log.debug("Expanding short code for redirect: {}", shortCode);
        UrlMapping mapping = getUrlMapping(shortCode);
        if (Instant.now().isAfter(mapping.getUpdatedAt().plus(config.getTtl()))) {
            log.warn("Link expired for short code: {}", shortCode);
            throw new LinkExpiredException("This link has expired");
        }
        return mapping.getFullUrl();
    }

    private UrlMapping getUrlMapping(String shortCode) {
        if (!isCorrectShortCode(shortCode)) {
            log.error("Invalid format of short code: {}", shortCode);
            throw new IllegalArgumentException("Short code must be exactly 8 characters long and contain only letters and digits: " + shortCode);
        }
        return repository.findById(shortCode)
                .orElseThrow(() -> {
                    log.error("Short link not found: {}", shortCode);
                    return new EntityNotFoundException("Short link not found: " + shortCode);
                });
    }

    private String generateCode(String url, int salt) {
        log.debug("Make an attempt to generate code: {}", url);
        String salted = url + "#" + salt;
        String hash = DigestUtils.md5DigestAsHex(salted.getBytes(StandardCharsets.UTF_8));
        return hash.substring(0, 8);
    }

    private String concatinateString(String first, String second) {
        return first + second;
    }

    private String getUrlPrefix() {
        return String.format("%s://%s:%s/%s/",
                config.getProtocol(),
                config.getHost(),
                config.getAllowedPorts().get(0),
                config.getPath());
    }

    private String extractShortCode(String inputUrl) {
        try {
            URI uri = new URI(inputUrl);

            if (!config.getProtocol().equals(uri.getScheme())) {
                log.error("Invalid protocol {}", inputUrl);
                throw new ParseShortCodeException(config.getProtocol(), "Invalid protocol");
            }
            if (!config.getHost().equals(uri.getHost())) {
                log.error("Invalid host {}", inputUrl);
                throw new ParseShortCodeException(config.getHost(), "Invalid host");
            }
            if (!config.getAllowedPorts().contains(String.valueOf(uri.getPort()))) {
                log.error("Invalid port {}", inputUrl);
                throw new ParseShortCodeException(String.join(", ", config.getAllowedPorts()), "Invalid port");
            }
            String path = uri.getPath();
            if (!path.startsWith("/" + config.getPath() + "/")) {
                log.error("Invalid path {}", inputUrl);
                throw new ParseShortCodeException("/" + config.getPath() +  "/", "Invalid path");
            }

            String[] parts = path.split("/");
            if (parts.length == 3) {
                log.info("Successfully expanded shortcode: {}", inputUrl);
                return parts[2];
            }

        }  catch (URISyntaxException e) {
            log.error("Invalid url {}", inputUrl);
            throw  new ParseShortCodeException("", "Invalid url");
        }
        log.error("Invalid url {}", inputUrl);
        throw  new ParseShortCodeException("", "Invalid url");
    }

    private boolean isCorrectShortCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{8}$");
    }

}