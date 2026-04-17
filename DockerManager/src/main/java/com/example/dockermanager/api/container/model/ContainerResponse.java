package com.example.dockermanager.api.container.model;

import com.example.dockermanager.domain.model.container.ContainerAction;
import com.example.dockermanager.domain.model.container.ContainerState;
import com.example.dockermanager.domain.model.container.ContainerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerResponse {
    private String id;
    private String name;
    private String image;
    private ContainerType containerType;
    private ContainerState state;
    private List<String> volumes;
    private List<ContainerAction> availableActions;
}
