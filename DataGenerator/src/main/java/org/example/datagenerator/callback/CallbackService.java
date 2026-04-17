package org.example.datagenerator.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.datagenerator.config.GeneratorProperties;
import org.example.datagenerator.model.GenerationState;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallbackService {

    private final RestTemplate restTemplate;
    private final GeneratorProperties props;

    static String normalizeCallbackUrl(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            throw new IllegalArgumentException("Callback URL is empty");
        }

        String normalized = callbackUrl.trim();
        if (normalized.contains("%")) {
            normalized = URLDecoder.decode(normalized, StandardCharsets.UTF_8);
        }

        while (normalized.startsWith(":")) {
            normalized = normalized.substring(1);
        }

        if (!(normalized.startsWith("http://") || normalized.startsWith("https://"))) {
            throw new IllegalArgumentException("Callback URL must start with http:// or https://");
        }

        return normalized;
    }

    public void reportProgress(int percent, String step, String message) {
        reportCallback(GenerationState.RUNNING, percent, step, message);
    }

    public void reportSuccess() {
        reportCallback(GenerationState.SUCCEEDED, 100, "COMPLETE", "Data generation completed");
    }

    public void reportFailure(String errorMessage) {
        reportCallback(GenerationState.FAILED, null, "FAILED", errorMessage);
    }

    private void reportCallback(GenerationState state, Integer percent, String step, String message) {
        String jobId = props.getJobId();
        try {
            var callback = ProgressCallback.builder()
                    .jobId(jobId)
                    .state(state)
                    .progressPercent(percent)
                    .currentStep(step)
                    .message(message)
                    .build();

            var headers = new HttpHeaders();
            headers.set("X-Generator-Token", props.getCallbackToken());
            headers.set("Content-Type", "application/json");

            var request = new HttpEntity<>(callback, headers);
            String callbackUrl = normalizeCallbackUrl(props.getCallbackUrl());
            warnIfLocalhostCallbackInContainer(jobId, callbackUrl);
            log.debug("[jobId={}] Sending callback to {}: state={}, step={}, progress={}%, message={}",
                    jobId, callbackUrl, state, step, percent, message);
            restTemplate.postForObject(URI.create(callbackUrl), request, Void.class);

            log.info("[jobId={}] Callback delivered: state={}, step={}, progress={}%, message={}",
                    jobId, state, step, percent, message);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("[jobId={}] Failed to send callback to {}: state={}, step={}, error={}",
                    jobId, props.getCallbackUrl(), state, step, e.getMessage());
        }
    }

    private void warnIfLocalhostCallbackInContainer(String jobId, String callbackUrl) {
        try {
            URI uri = URI.create(callbackUrl);
            String host = uri.getHost();
            boolean localhostTarget = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
            boolean inContainer = Files.exists(Path.of("/.dockerenv"));

            if (localhostTarget && inContainer) {
                log.warn("[jobId={}] Callback URL points to localhost from inside a container ({}). " +
                                "This resolves to the generator container itself and is usually unreachable for DockerManager. " +
                                "Set GENERATOR_CALLBACK_URL to DockerManager service DNS (for example http://docker-manager:8000/...) " +
                                "or host gateway address (for example http://host.docker.internal:8000/...).",
                        jobId, callbackUrl);
            }
        } catch (IllegalArgumentException ignored) {
            // URL parsing errors are already handled by normalizeCallbackUrl/caller.
        }
    }
}
