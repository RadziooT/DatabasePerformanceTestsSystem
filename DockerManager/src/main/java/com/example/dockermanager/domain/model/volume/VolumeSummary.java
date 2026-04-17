package com.example.dockermanager.domain.model.volume;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VolumeSummary {
    String name;
    String mountpoint;
    VolumeType volumeType;
    VolumeStatus status;
}
