package com.example.dockermanager.api.container.mapper;

import com.example.dockermanager.api.container.model.ContainerResponse;
import com.example.dockermanager.domain.model.container.ContainerSummary;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ContainerResponseMapper {

    public ContainerResponse toResponse(ContainerSummary summary) {
        return ContainerResponse.builder()
                .id(summary.getId())
                .name(summary.getName())
                .image(summary.getImage())
                .containerType(summary.getContainerType())
                .state(summary.getState())
                .volumes(summary.getVolumes())
                .availableActions(summary.getAvailableActions())
                .build();
    }
}
