package org.example.datagenerator.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.example.datagenerator.generation.strategy.dataGeneration.*;
import org.example.datagenerator.generation.strategy.schemaGeneration.*;
import org.example.datagenerator.model.DatabaseType;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class GeneratorConfig {

    @Bean
    public DataSource dataSource(GeneratorProperties props) {
        String url = buildJdbcUrl(props);
        log.info("[jobId={}] Initializing datasource: dbType={}, driver={}, url={}, user={}",
                props.getJobId(), props.getDbType(), getDriverClassName(props.getDbType()), url, props.getDbUser());
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(getDriverClassName(props.getDbType()))
                .url(url)
                .username(props.getDbUser())
                .password(props.getDbPassword())
                .build();

        dataSource.setMinimumIdle(props.getPoolMinimumIdle());
        dataSource.setMaximumPoolSize(props.getPoolMaximumSize());
        dataSource.setConnectionTimeout(props.getPoolConnectionTimeoutMs());
        dataSource.setIdleTimeout(props.getPoolIdleTimeoutMs());
        dataSource.setMaxLifetime(props.getPoolMaxLifetimeMs());
        dataSource.setPoolName("generator-pool");

        return dataSource;
    }

    @Bean
    public SchemaGenerationStrategy schemaGenerationStrategy(GeneratorProperties props, DataSource dataSource) {
        return switch (props.getDbType()) {
            case POSTGRES -> new PostgresSchemaGenerator(dataSource);
            case MYSQL -> new MysqlSchemaGenerator(dataSource);
            case ORACLE -> new OracleSchemaGenerator(dataSource);
            case SQLSERVER -> new SqlServerSchemaGenerator(dataSource);
        };
    }

    @Bean
    public DataGenerationStrategy dataGenerationStrategy(GeneratorProperties props, DataSource dataSource) {
        return switch (props.getDbType()) {
            case POSTGRES -> new PostgresGenerationStrategy(dataSource, props.getBatchSize());
            case MYSQL -> new MysqlGenerationStrategy(dataSource, props.getBatchSize());
            case ORACLE -> new OracleGenerationStrategy(dataSource, props.getBatchSize());
            case SQLSERVER -> new SqlServerGenerationStrategy(dataSource, props.getBatchSize());
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String buildJdbcUrl(GeneratorProperties props) {
        return switch (props.getDbType()) {
            case POSTGRES -> String.format("jdbc:postgresql://%s:%d/%s?reWriteBatchedInserts=true",
                    props.getDbNetworkName(), props.getDbPort(), props.getDbName());
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC&rewriteBatchedStatements=true&useServerPrepStmts=false&allowLoadLocalInfile=true",
                    props.getDbNetworkName(), props.getDbPort(), props.getDbName());
            case ORACLE -> String.format("jdbc:oracle:thin:@//%s:%d/%s",
                    props.getDbNetworkName(), props.getDbPort(), props.getDbName());
            case SQLSERVER ->
                    String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true",
                            props.getDbNetworkName(), props.getDbPort(), props.getDbName());
        };
    }

    private String getDriverClassName(DatabaseType dbType) {
        return switch (dbType) {
            case POSTGRES -> "org.postgresql.Driver";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case ORACLE -> "oracle.jdbc.OracleDriver";
            case SQLSERVER -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        };
    }
}
