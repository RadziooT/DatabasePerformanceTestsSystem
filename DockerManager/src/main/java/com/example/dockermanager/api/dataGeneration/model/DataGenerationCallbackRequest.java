package com.example.dockermanager.api.dataGeneration.model;

import com.example.dockermanager.domain.model.dataGeneration.DataGenerationState;
import lombok.Data;

@Data
public class DataGenerationCallbackRequest {
    private String jobId;
    private DataGenerationState state;
    private Integer progressPercent;
    private String currentStep;
    private String message;
}
