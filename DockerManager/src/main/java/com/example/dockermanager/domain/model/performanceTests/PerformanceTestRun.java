package com.example.dockermanager.domain.model.performanceTests;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PerformanceTestRun {
    String runId;
    Instant startedAt;
}
