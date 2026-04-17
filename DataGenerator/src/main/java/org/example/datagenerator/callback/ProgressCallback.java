package org.example.datagenerator.callback;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.example.datagenerator.model.GenerationState;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressCallback {
    private String jobId;
    private GenerationState state;
    private Integer progressPercent;
    private String currentStep;
    private String message;
}
