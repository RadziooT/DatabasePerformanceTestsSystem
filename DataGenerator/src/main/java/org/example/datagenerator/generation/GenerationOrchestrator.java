package org.example.datagenerator.generation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.datagenerator.callback.CallbackService;
import org.example.datagenerator.config.ConfigurationValidator;
import org.example.datagenerator.config.GeneratorProperties;
import org.example.datagenerator.generation.strategy.dataGeneration.DataGenerationStrategy;
import org.example.datagenerator.generation.strategy.schemaGeneration.SchemaGenerationStrategy;
import org.example.datagenerator.model.DataGenerationRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenerationOrchestrator {

    private final GeneratorProperties props;
    private final SchemaGenerationStrategy schemaStrategy;
    private final DataGenerationStrategy strategy;
    private final CallbackService callbackService;
    private final ConfigurationValidator validator;

    public void generateData() {
        String jobId = props.getJobId();
        try {
            validator.validateConfiguration(props);

            log.info("[jobId={}] Starting TPC-C data generation: dbType={}, volumeType={}, batchSize={}",
                    jobId, props.getDbType(), props.getVolumeType(), props.getBatchSize());
            log.info("[jobId={}] Database connection target: {}:{}@{}/{}",
                    jobId, props.getDbNetworkName(), props.getDbPort(), props.getDbUser(), props.getDbName());

            callbackService.reportProgress(0, "SCHEMA_INIT", "Creating TPC-C schema");
            log.info("[jobId={}] Step SCHEMA_INIT started - creating schema", jobId);
            schemaStrategy.createSchema();
            log.info("[jobId={}] Step SCHEMA_INIT completed - schema created", jobId);

            callbackService.reportProgress(5, "SCHEMA_CREATED", "Schema ready, starting data generation");
            log.info("[jobId={}] Step DATA_GENERATION started", jobId);
            strategy.generateData(new DataGenerationRequest(props.getVolumeType(), (percent, step, msg) -> {
                log.debug("[jobId={}] Progress update: {}% {} - {}", jobId, percent, step, msg);
                callbackService.reportProgress(percent, step, msg);
            }));

            callbackService.reportSuccess();
            log.info("[jobId={}] Data generation completed successfully", jobId);
        } catch (Exception e) {
            log.error("[jobId={}] Data generation failed", jobId, e);
            callbackService.reportFailure("Data generation failed: " + e.getMessage());
            throw e;
        }
    }
}
