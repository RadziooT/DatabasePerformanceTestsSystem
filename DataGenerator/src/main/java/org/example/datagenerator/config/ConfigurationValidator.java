package org.example.datagenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigurationValidator {
    public void validateConfiguration(GeneratorProperties props) {
        String jobId = props.getJobId();
        if (props.getDbNetworkName() == null || props.getDbNetworkName().isBlank()) {
            String error = "Missing required database network name. Please set GENERATOR_DB_NETWORK_NAME environment variable.";
            log.error("[jobId={}] Configuration validation failed: {}", jobId, error);
            throw new IllegalArgumentException(error);
        }
        if (props.getDbPort() <= 0) {
            String error = "Missing or invalid database port. Please set GENERATOR_DB_PORT environment variable to a valid port (1-65535).";
            log.error("[jobId={}] Configuration validation failed: {}", jobId, error);
            throw new IllegalArgumentException(error);
        }
        if (props.getDbPort() > 65535) {
            String error = "Invalid database port: " + props.getDbPort() + ". Port must be between 1 and 65535. Check GENERATOR_DB_PORT.";
            log.error("[jobId={}] Configuration validation failed: {}", jobId, error);
            throw new IllegalArgumentException(error);
        }
        if (props.getDbName() == null || props.getDbName().isBlank()) {
            String error = "Missing required database name/service. Please set GENERATOR_DB_NAME environment variable.";
            log.error("[jobId={}] Configuration validation failed: {}", jobId, error);
            throw new IllegalArgumentException(error);
        }
        if (props.getVolumeType() == null) {
            String error = "Missing required volume type. Please set GENERATOR_VOLUME_TYPE to SMALL, MEDIUM, or LARGE.";
            log.error("[jobId={}] Configuration validation failed: {}", jobId, error);
            throw new IllegalArgumentException(error);
        }
        log.info("[jobId={}] Configuration validation passed: dbType={}, dbNetworkName={}, port={}, database={}, volumeType={}",
                jobId, props.getDbType(), props.getDbNetworkName(), props.getDbPort(), props.getDbName(), props.getVolumeType());
    }
}
