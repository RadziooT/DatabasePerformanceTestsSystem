package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.HealthCheck;
import com.github.dockerjava.api.model.RestartPolicy;

import java.util.List;

public abstract class DatabaseContainerConfiguration extends ContainerConfiguration {

    protected static final long DATABASE_MEMORY_LIMIT_BYTES = 3_221_225_472L; // 3GB
    protected static final long DATABASE_CPU_COUNT = 2L;
    protected static final long DATABASE_HEALTHCHECK_INTERVAL_NANOS = 10_000_000_000L;
    protected static final long DATABASE_HEALTHCHECK_TIMEOUT_NANOS = 10_000_000_000L;
    protected static final int DATABASE_HEALTHCHECK_RETRIES = 5;
    protected static final long ORACLE_HEALTHCHECK_START_PERIOD_NANOS = 90_000_000_000L;

    protected DatabaseContainerConfiguration(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected void applyCustomHostConfig(HostConfig hostConfig) {
        hostConfig
                .withMemory(DATABASE_MEMORY_LIMIT_BYTES)
                .withMemorySwap(DATABASE_MEMORY_LIMIT_BYTES)
                .withCpuCount(DATABASE_CPU_COUNT)
                .withCpuPeriod(100_000L)
                .withCpuQuota(200_000L)
                .withRestartPolicy(RestartPolicy.unlessStoppedRestart());
        applyAdditionalHostConfig(hostConfig);
    }

    protected void applyAdditionalHostConfig(HostConfig hostConfig) {
        // Hook for database-specific host tweaks.
    }

    protected HealthCheck buildHealthCheck(String testCommand) {
        return buildHealthCheck(testCommand, null);
    }

    protected HealthCheck buildHealthCheck(String testCommand, Long startPeriodNanos) {
        HealthCheck healthCheck = new HealthCheck()
                .withTest(List.of("CMD-SHELL", testCommand))
                .withInterval(DATABASE_HEALTHCHECK_INTERVAL_NANOS)
                .withTimeout(DATABASE_HEALTHCHECK_TIMEOUT_NANOS)
                .withRetries(DATABASE_HEALTHCHECK_RETRIES);

        if (startPeriodNanos != null) {
            healthCheck.withStartPeriod(startPeriodNanos);
        }

        return healthCheck;
    }
}
