package org.example.datagenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.example.datagenerator.callback.CallbackService;
import org.example.datagenerator.generation.GenerationOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
public class StartupRunnerConfig {

    @Bean
    public CommandLineRunner startupGeneratorRunner(
            GenerationOrchestrator orchestrator,
            GeneratorProperties props,
            ApplicationContext context,
            CallbackService callbackService
    ) {
        return args -> {
            AtomicInteger exitCode = new AtomicInteger(0);
            String jobId = props.getJobId();

            try {
                log.info("[jobId={}] Startup runner is starting data generation pipeline", jobId);
                orchestrator.generateData();
                log.info("[jobId={}] Startup runner finished data generation successfully", jobId);
            } catch (Exception e) {
                log.error("[jobId={}] Startup runner failed during data generation", jobId, e);
                try {
                    callbackService.reportFailure("Startup runner failed: " + e.getMessage());
                } catch (Exception callbackEx) {
                    log.warn("[jobId={}] Failed to send fallback failure callback from startup runner", jobId, callbackEx);
                }
                exitCode.set(1);
            }

            shutdown(context, exitCode.get(), jobId);
        };
    }

    protected void shutdown(ApplicationContext context, int exitCode, String jobId) {
        int appExitCode = SpringApplication.exit(context, () -> exitCode);
        log.info("[jobId={}] Exiting application with code {}", jobId, appExitCode);
        System.exit(appExitCode);
    }
}
