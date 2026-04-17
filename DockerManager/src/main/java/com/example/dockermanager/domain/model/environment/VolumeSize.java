package com.example.dockermanager.domain.model.environment;

import java.util.Arrays;
import java.util.List;

public enum VolumeSize {
    SMALL,
    MEDIUM,
    LARGE;

    public static List<String> getNames() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
