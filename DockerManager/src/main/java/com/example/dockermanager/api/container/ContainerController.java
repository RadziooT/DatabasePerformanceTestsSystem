package com.example.dockermanager.api.container;

import com.example.dockermanager.api.container.mapper.ContainerResponseMapper;
import com.example.dockermanager.api.container.model.ContainerResponse;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.service.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    @GetMapping
    public ResponseEntity<List<ContainerResponse>> getAllContainers() {
        return ResponseEntity.ok(containerService.listContainers().stream()
                .map(ContainerResponseMapper::toResponse)
                .toList());
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopAllContainers() {
        containerService.stopAllContainers();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop/{containerType}")
    public ResponseEntity<?> stopContainer(@PathVariable ContainerType containerType) {
        containerService.stopContainer(containerType);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{containerType}")
    public ResponseEntity<?> deleteContainer(@PathVariable ContainerType containerType) {
        containerService.removeContainer(containerType);
        return ResponseEntity.ok().build();
    }
}
