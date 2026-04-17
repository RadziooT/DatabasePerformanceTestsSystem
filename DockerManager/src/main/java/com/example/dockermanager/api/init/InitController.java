package com.example.dockermanager.api.init;

import com.example.dockermanager.api.init.model.InitApplicationOptionsResponse;
import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.PerformanceSimulationType;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InitController {

    @GetMapping("/init")
    public ResponseEntity<InitApplicationOptionsResponse> getAvailableOptions() {
        var options = InitApplicationOptionsResponse.builder()
                .databaseTypes(DatabaseType.getNames())
                .volumeTypes(VolumeSize.getNames())
                .performanceTestTypes(PerformanceSimulationType.getNames())
                .build();
        return ResponseEntity.ok().body(options);
    }
}


