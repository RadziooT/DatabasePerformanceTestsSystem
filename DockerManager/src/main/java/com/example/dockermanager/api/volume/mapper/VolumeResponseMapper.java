package com.example.dockermanager.api.volume.mapper;

import com.example.dockermanager.api.volume.model.VolumeResponse;
import com.example.dockermanager.domain.model.volume.VolumeSummary;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VolumeResponseMapper {

    public VolumeResponse toResponse(VolumeSummary summary) {
        return VolumeResponse.builder()
                .name(summary.getName())
                .mountpoint(summary.getMountpoint())
                .volumeType(summary.getVolumeType())
                .status(summary.getStatus())
                .build();
    }
}
