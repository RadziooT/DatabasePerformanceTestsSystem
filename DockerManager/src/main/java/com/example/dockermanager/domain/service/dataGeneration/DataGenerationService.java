package com.example.dockermanager.domain.service.dataGeneration;

import com.example.dockermanager.domain.exception.DataGenerationJobAlreadyRunningException;
import com.example.dockermanager.domain.exception.DataGenerationPrerequisiteException;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.DataGenerationContainerParams;
import com.example.dockermanager.domain.model.dataGeneration.*;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.example.dockermanager.domain.service.container.ContainerService;
import com.example.dockermanager.domain.service.util.DatabaseResourceMappingService;
import com.example.dockermanager.domain.service.volume.VolumeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataGenerationService {

    private final ContainerService containerService;
    private final VolumeUtil volumeUtil;
    private final DatabaseResourceMappingService databaseResourceMappingService;
    private final Object lock = new Object();
    @Value("${data-generation.callback-token:docker-manager-dev-token}")
    private String callbackToken;
    private DataGenerationJob currentJob;

    public DataGenerationStatus startJob(DataGenerationStartCommand request) {
        validateRequest(request);

        synchronized (lock) {
            if (currentJob != null && currentJob.getState() == DataGenerationState.RUNNING) {
                throw new DataGenerationJobAlreadyRunningException("A data generation job is already running");
            }

            var containerType = databaseResourceMappingService.toContainerType(request.databaseType());
            var volumeType = databaseResourceMappingService.toVolumeType(request.databaseType(), request.volumeType());
            validatePrerequisites(containerType, volumeType);

            String jobId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            currentJob = DataGenerationJob.builder()
                    .jobId(jobId)
                    .state(DataGenerationState.RUNNING)
                    .progressPercent(0)
                    .startedAt(now)
                    .build();

            try {
                if (containerService.isContainerRunning(ContainerType.DATA_GENERATOR)) {
                    containerService.stopContainer(ContainerType.DATA_GENERATOR);
                }
                if (containerService.isContainerCreated(ContainerType.DATA_GENERATOR)) {
                    containerService.removeContainer(ContainerType.DATA_GENERATOR);
                }

                var containerParams = new DataGenerationContainerParams(jobId, request.databaseType().toContainerType(), request.volumeType());
                containerService.createDataGenerationContainer(ContainerType.DATA_GENERATOR, containerParams);
                containerService.startContainer(ContainerType.DATA_GENERATOR);

                return toResponse(currentJob);
            } catch (RuntimeException e) {
                currentJob.setState(DataGenerationState.FAILED);
                currentJob.setFinishedAt(Instant.now());
                throw e;
            }
        }
    }

    public DataGenerationStatus getStatus() {
        synchronized (lock) {
            if (currentJob == null) {
                return DataGenerationStatus.builder()
                        .state(DataGenerationState.IDLE)
                        .progressPercent(0)
                        .currentStep("IDLE")
                        .startedAt(null)
                        .finishedAt(null)
                        .build();
            }
            return toResponse(currentJob);
        }
    }

    public boolean applyCallback(String requestToken, DataGenerationCallbackCommand callbackRequest) {
        if (!Objects.equals(callbackToken, requestToken)) {
            throw new SecurityException("Invalid callback token");
        }
        if (callbackRequest == null || callbackRequest.jobId() == null || callbackRequest.jobId().isBlank()) {
            throw new IllegalArgumentException("Callback request must contain jobId");
        }

        synchronized (lock) {
            if (currentJob == null) {
                return false;
            }
            if (!currentJob.getJobId().equals(callbackRequest.jobId())) {
                return false;
            }
            if (currentJob.getState().isTerminal()) {
                return false;
            }

            if (callbackRequest.progressPercent() != null) {
                currentJob.setProgressPercent(callbackRequest.progressPercent());
            }
            if (callbackRequest.currentStep() != null) {
                currentJob.setCurrentStep(callbackRequest.currentStep());
            }
            if (callbackRequest.state() != null) {
                currentJob.setState(callbackRequest.state());
                if (currentJob.getState().isTerminal()) {
                    if (currentJob.getState() == DataGenerationState.SUCCEEDED && currentJob.getProgressPercent() < 100) {
                        currentJob.setProgressPercent(100);
                    }
                    currentJob.setFinishedAt(Instant.now());
                }
            }

            log.debug("Received generation callback for job {} with progress {}%", currentJob.getJobId(), currentJob.getProgressPercent());
            return true;
        }
    }

    private void validateRequest(DataGenerationStartCommand request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.databaseType() == null) {
            throw new IllegalArgumentException("databaseType is required");
        }
        if (request.volumeType() == null) {
            throw new IllegalArgumentException("volumeType is required");
        }
    }

    private void validatePrerequisites(ContainerType containerType, VolumeType volumeType) {
        if (!containerService.isContainerRunning(containerType)) {
            throw new DataGenerationPrerequisiteException("Required database container is not running: " + containerType.getContainerName());
        }
        if (!volumeUtil.isVolumePresent(volumeType)) {
            throw new DataGenerationPrerequisiteException("Required database volume does not exist: " + volumeType.getVolumeName());
        }
        if (!containerService.isVolumeMounted(containerType, volumeType)) {
            throw new DataGenerationPrerequisiteException("Required database volume is not mounted by running database container: " + volumeType.getVolumeName());
        }
    }

    private DataGenerationStatus toResponse(DataGenerationJob job) {
        return DataGenerationStatus.builder()
                .state(job.getState())
                .progressPercent(job.getProgressPercent())
                .currentStep(job.getCurrentStep())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}
