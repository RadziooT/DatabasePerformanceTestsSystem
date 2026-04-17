package com.example.dockermanager.domain.service.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerNetworkService {

    private static final String SHARED_NETWORK_NAME = "docker-manager-network";

    private final DockerClient dockerClient;

    private void ensureSharedNetworkExists() {
        try {
            dockerClient.inspectNetworkCmd().withNetworkId(SHARED_NETWORK_NAME).exec();
            log.debug("Shared network {} already exists", SHARED_NETWORK_NAME);
        } catch (NotFoundException e) {
            log.info("Creating shared network: {}", SHARED_NETWORK_NAME);
            dockerClient.createNetworkCmd()
                    .withName(SHARED_NETWORK_NAME)
                    .withDriver("bridge")
                    .exec();
        }
    }

    public void withCustomSharedNetwork(HostConfig hostConfig) {
        ensureSharedNetworkExists();
        hostConfig.withNetworkMode(SHARED_NETWORK_NAME);
    }
}
