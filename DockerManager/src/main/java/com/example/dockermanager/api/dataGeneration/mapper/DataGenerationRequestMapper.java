package com.example.dockermanager.api.dataGeneration.mapper;

import com.example.dockermanager.api.dataGeneration.model.DataGenerationCallbackRequest;
import com.example.dockermanager.api.dataGeneration.model.DataGenerationStartRequest;
import com.example.dockermanager.domain.model.dataGeneration.DataGenerationCallbackCommand;
import com.example.dockermanager.domain.model.dataGeneration.DataGenerationStartCommand;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataGenerationRequestMapper {

    public DataGenerationStartCommand toDataGenerationStartCommand(DataGenerationStartRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        return new DataGenerationStartCommand(
                request.getDatabaseType(),
                request.getVolumeType()
        );
    }

    public DataGenerationCallbackCommand toDataGenerationCallbackCommand(DataGenerationCallbackRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        return new DataGenerationCallbackCommand(
                request.getJobId(),
                request.getState(),
                request.getProgressPercent(),
                request.getCurrentStep(),
                request.getMessage()
        );
    }
}
