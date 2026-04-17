package com.example.dockermanager.api.environment;

import com.example.dockermanager.api.environment.mapper.EnvironmentRequestMapper;
import com.example.dockermanager.api.environment.model.EnvironmentRunRequest;
import com.example.dockermanager.domain.service.environmentStartup.EnvironmentStartupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentStartupService environmentStartupService;

    @PostMapping("/start")
    public ResponseEntity<?> startEnvironment(@RequestBody EnvironmentRunRequest request) {
        environmentStartupService.startEnvironment(EnvironmentRequestMapper.toRunEnvironmentConfig(request));
        return ResponseEntity.ok().build();
    }
}
