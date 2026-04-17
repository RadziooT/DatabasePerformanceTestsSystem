package com.example.dockermanager.api.dataGeneration.model;

import com.example.dockermanager.domain.model.dataGeneration.DataGenerationState;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DataGenerationStatusResponse {
    DataGenerationState state;
    int progressPercent;
    String currentStep;
    Instant startedAt;
    Instant finishedAt;
}
