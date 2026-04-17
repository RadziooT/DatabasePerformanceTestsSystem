package com.example.dockermanager.domain.service.volume;

import com.example.dockermanager.domain.model.volume.VolumeStatus;
import com.example.dockermanager.domain.model.volume.VolumeSummary;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VolumeService {

    private final DockerClient dockerClient;

    public List<VolumeSummary> listVolumes() {
        var volumes = findManagedVolumes();

        return Arrays.stream(VolumeType.values()).map(volumeType -> {
                    var volumeData = volumes.stream()
                            .filter(availableVolume -> volumeType.getVolumeName().equals(availableVolume.getName()))
                            .findFirst();
                    return volumeData.isPresent()
                            ? mapPresentVolumeInfo(volumeData.get(), volumeType)
                            : mapEmptyVolumeInfo(volumeType);
                })
                .toList();
    }

    public void createVolume(VolumeType volumeType) {
        dockerClient.createVolumeCmd().withName(volumeType.getVolumeName()).exec();
    }

    public void deleteAllVolumes() {
        findManagedVolumes()
                .forEach(volume -> {
                    log.info("Removing volume {}", volume.getName());
                    dockerClient.removeVolumeCmd(volume.getName()).exec();
                });
    }

    public void deleteVolume(VolumeType volumeType) {
        dockerClient.removeVolumeCmd(volumeType.getVolumeName()).exec();
    }

    private VolumeSummary mapPresentVolumeInfo(InspectVolumeResponse volumeData, VolumeType volumeType) {
        return VolumeSummary.builder()
                .name(volumeData.getName())
                .mountpoint(volumeData.getMountpoint())
                .status(VolumeStatus.CREATED)
                .volumeType(volumeType)
                .build();
    }

    private VolumeSummary mapEmptyVolumeInfo(VolumeType volumeType) {
        return VolumeSummary.builder()
                .name(volumeType.getVolumeName())
                .status(VolumeStatus.NOT_CREATED)
                .volumeType(volumeType)
                .build();
    }

    private List<InspectVolumeResponse> findManagedVolumes() {
        return dockerClient.listVolumesCmd().exec().getVolumes()
                .stream()
                .toList();
    }
}
