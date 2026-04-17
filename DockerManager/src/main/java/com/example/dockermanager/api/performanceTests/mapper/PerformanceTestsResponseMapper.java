package com.example.dockermanager.api.performanceTests.mapper;

import com.example.dockermanager.api.performanceTests.model.PerformanceTestRunResponse;
import com.example.dockermanager.domain.model.performanceTests.PerformanceTestRun;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PerformanceTestsResponseMapper {

    public PerformanceTestRunResponse toPerformanceTestRunResponse(PerformanceTestRun run) {
        return PerformanceTestRunResponse.builder()
                .runId(run.getRunId())
                .startedAt(run.getStartedAt())
                .build();
    }
}

