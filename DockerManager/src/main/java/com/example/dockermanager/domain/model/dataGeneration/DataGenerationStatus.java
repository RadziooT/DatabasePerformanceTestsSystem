package com.example.dockermanager.domain.model.dataGeneration;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DataGenerationStatus {
    DataGenerationState state;
    int progressPercent;
    String currentStep;
    Instant startedAt;
    Instant finishedAt;
}
