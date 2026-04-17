package com.example.dockermanager.domain.model.dataGeneration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DataGenerationJob {
    private final String jobId;
    private final Instant startedAt;
    private DataGenerationState state;
    private int progressPercent;
    private String currentStep;
    private Instant finishedAt;
}
