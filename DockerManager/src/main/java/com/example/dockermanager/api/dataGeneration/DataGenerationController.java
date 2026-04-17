package com.example.dockermanager.api.dataGeneration;

import com.example.dockermanager.api.dataGeneration.mapper.DataGenerationRequestMapper;
import com.example.dockermanager.api.dataGeneration.mapper.DataGenerationResponseMapper;
import com.example.dockermanager.api.dataGeneration.model.DataGenerationCallbackRequest;
import com.example.dockermanager.api.dataGeneration.model.DataGenerationStartRequest;
import com.example.dockermanager.api.dataGeneration.model.DataGenerationStatusResponse;
import com.example.dockermanager.domain.exception.DataGenerationJobAlreadyRunningException;
import com.example.dockermanager.domain.exception.DataGenerationPrerequisiteException;
import com.example.dockermanager.domain.service.dataGeneration.DataGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data-generation")
@RequiredArgsConstructor
public class DataGenerationController {

    private final DataGenerationService dataGenerationService;

    @PostMapping("/start")
    public ResponseEntity<?> startDataGeneration(@RequestBody DataGenerationStartRequest request) {
        try {
            DataGenerationStatusResponse status = DataGenerationResponseMapper.toDataGenerationStatusResponse(
                    dataGenerationService.startJob(DataGenerationRequestMapper.toDataGenerationStartCommand(request))
            );
            return ResponseEntity.accepted().body(status);
        } catch (DataGenerationJobAlreadyRunningException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (DataGenerationPrerequisiteException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<DataGenerationStatusResponse> getDataGenerationStatus() {
        return ResponseEntity.ok(DataGenerationResponseMapper.toDataGenerationStatusResponse(dataGenerationService.getStatus()));
    }

    @PostMapping("/callback")
    public ResponseEntity<?> dataGenerationCallback(@RequestHeader(name = "X-Generator-Token", required = false) String callbackToken,
                                                    @RequestBody DataGenerationCallbackRequest request) {
        try {
            boolean accepted = dataGenerationService.applyCallback(
                    callbackToken,
                    DataGenerationRequestMapper.toDataGenerationCallbackCommand(request)
            );
            if (!accepted) {
                return ResponseEntity.accepted().build();
            }
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
