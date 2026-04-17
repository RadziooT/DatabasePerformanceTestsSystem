package com.example.dockermanager.api.volume.model;

import com.example.dockermanager.domain.model.volume.VolumeStatus;
import com.example.dockermanager.domain.model.volume.VolumeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeResponse {
    private String name;
    private String mountpoint;
    private VolumeType volumeType;
    private VolumeStatus status;
}
