package com.example.dockermanager.api.volume;

import com.example.dockermanager.api.volume.mapper.VolumeResponseMapper;
import com.example.dockermanager.api.volume.model.VolumeResponse;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.example.dockermanager.domain.service.volume.VolumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volumes")
@RequiredArgsConstructor
public class VolumeController {

    private final VolumeService volumeService;

    @GetMapping
    public ResponseEntity<List<VolumeResponse>> getVolumes() {
        return ResponseEntity.ok(volumeService.listVolumes().stream()
                .map(VolumeResponseMapper::toResponse)
                .toList());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAllVolumes() {
        volumeService.deleteAllVolumes();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{volumeType}")
    public ResponseEntity<?> deleteVolume(@PathVariable VolumeType volumeType) {
        volumeService.deleteVolume(volumeType);
        return ResponseEntity.ok().build();
    }
}
