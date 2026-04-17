package org.example.datagenerator.config;

import lombok.Data;
import org.example.datagenerator.model.DatabaseType;
import org.example.datagenerator.model.VolumeSize;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "generator")
@Data
public class GeneratorProperties {
    private String jobId;
    private DatabaseType dbType;
    private VolumeSize volumeType = VolumeSize.SMALL;
    private String dbNetworkName;
    private int dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private String callbackUrl;
    private String callbackToken;

    private int batchSize;
    private int poolMinimumIdle;
    private int poolMaximumSize;
    private long poolConnectionTimeoutMs;
    private long poolIdleTimeoutMs;
    private long poolMaxLifetimeMs;
}
