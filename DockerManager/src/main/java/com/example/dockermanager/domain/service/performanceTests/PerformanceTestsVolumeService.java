package com.example.dockermanager.domain.service.performanceTests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service to copy Gatling results from Docker volume to local filesystem.
 * This is necessary because Docker named volumes on Windows are not directly accessible from host.
 */
@Service
@Slf4j
public class PerformanceTestsVolumeService {

    private static final String GATLING_VOLUME_NAME = "gatling-results-local";
    private final AtomicLong lastSuccessfulSyncEpochMs = new AtomicLong(0);

    public void syncResultsFromVolumeIfStale(Path targetDirectory, Duration minInterval) {
        long now = System.currentTimeMillis();
        long lastSync = lastSuccessfulSyncEpochMs.get();
        if (now - lastSync < minInterval.toMillis()) {
            return;
        }

        syncResultsFromVolume(targetDirectory);
    }

    /**
     * Copy Gatling results from Docker named volume to local directory using docker run.
     */
    public void syncResultsFromVolume(Path targetDirectory) {
        String tempContainerName = "gatling-sync-tmp-" + System.currentTimeMillis();
        try {
            // Create target directory if it doesn't exist
            Files.createDirectories(targetDirectory);

            CommandResult create = runDockerCommand(List.of(
                    "docker", "create", "--name", tempContainerName,
                    "-v", GATLING_VOLUME_NAME + ":/data",
                    "alpine:latest", "sh", "-c", "sleep 10"
            ));
            if (create.exitCode != 0) {
                log.warn("Cannot create temp container for Gatling volume sync: {}", create.output);
                return;
            }

            CommandResult copy = runDockerCommand(List.of(
                    "docker", "cp",
                    tempContainerName + ":/data/.",
                    targetDirectory.toAbsolutePath().toString()
            ));

            if (copy.exitCode == 0) {
                lastSuccessfulSyncEpochMs.set(System.currentTimeMillis());
                log.info("Successfully synced Gatling results from volume {} to {}", GATLING_VOLUME_NAME, targetDirectory);
            } else {
                log.warn("Failed to sync Gatling results from volume {}: {}", GATLING_VOLUME_NAME, copy.output);
            }
        } catch (IOException e) {
            log.error("Failed to execute docker command to sync Gatling results", e);
        } finally {
            try {
                runDockerCommand(List.of("docker", "rm", "-f", tempContainerName));
            } catch (IOException e) {
                log.debug("Unable to remove temporary sync container {}", tempContainerName, e);
            }
        }
    }

    private CommandResult runDockerCommand(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        try {
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output.trim());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while executing docker command", e);
        }
    }

    private record CommandResult(int exitCode, String output) {
    }
}
