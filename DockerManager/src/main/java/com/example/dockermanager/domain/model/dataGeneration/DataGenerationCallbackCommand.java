package com.example.dockermanager.domain.model.dataGeneration;

public record DataGenerationCallbackCommand(
        String jobId,
        DataGenerationState state,
        Integer progressPercent,
        String currentStep,
        String message
) {
}
