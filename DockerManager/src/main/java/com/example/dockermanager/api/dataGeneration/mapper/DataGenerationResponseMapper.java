package com.example.dockermanager.api.dataGeneration.mapper;

import com.example.dockermanager.api.dataGeneration.model.DataGenerationStatusResponse;
import com.example.dockermanager.domain.model.dataGeneration.DataGenerationStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DataGenerationResponseMapper {

    public DataGenerationStatusResponse toDataGenerationStatusResponse(DataGenerationStatus status) {
        return DataGenerationStatusResponse.builder()
                .state(status.getState())
                .progressPercent(status.getProgressPercent())
                .currentStep(status.getCurrentStep())
                .startedAt(status.getStartedAt())
                .finishedAt(status.getFinishedAt())
                .build();
    }
}
