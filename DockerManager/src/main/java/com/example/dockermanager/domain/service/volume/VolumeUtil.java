package com.example.dockermanager.domain.service.volume;

import com.example.dockermanager.domain.model.volume.VolumeType;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolumeUtil {

    private final DockerClient dockerClient;

    public boolean isVolumePresent(VolumeType volumeType) {
        try {
            dockerClient.inspectVolumeCmd(volumeType.getVolumeName()).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
