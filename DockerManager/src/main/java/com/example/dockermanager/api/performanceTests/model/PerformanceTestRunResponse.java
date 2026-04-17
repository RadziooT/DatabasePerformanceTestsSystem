package com.example.dockermanager.api.performanceTests.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PerformanceTestRunResponse {
    String runId;
    Instant startedAt;
}
