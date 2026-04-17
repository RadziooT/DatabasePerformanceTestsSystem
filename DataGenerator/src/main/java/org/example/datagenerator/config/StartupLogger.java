package org.example.datagenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StartupLogger {

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        logEnvironmentVariables();
    }

    private void logEnvironmentVariables() {
        log.info("======================================");
        log.info("DataGenerator Startup Configuration");
        log.info("======================================");

        Map<String, String> env = System.getenv();
        Map<String, String> generatorVars = env.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("GENERATOR_"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (generatorVars.isEmpty()) {
            log.warn("No GENERATOR_* environment variables found!");
        } else {
            log.info("Detected {} GENERATOR_* environment variables:", generatorVars.size());
            generatorVars.forEach((key, value) -> {
                if (key.toLowerCase().contains("password")) {
                    log.info("  {} = ***[MASKED]***", key);
                } else {
                    log.info("  {} = {}", key, value);
                }
            });
        }

        log.info("======================================");
    }
}
