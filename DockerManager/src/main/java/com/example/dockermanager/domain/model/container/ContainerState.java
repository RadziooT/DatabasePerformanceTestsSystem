package com.example.dockermanager.domain.model.container;

public enum ContainerState {
    CREATED,
    RESTARTING,
    RUNNING,
    REMOVING,
    PAUSED,
    EXITED,
    DEAD,
    STOPPED,
    NOT_CREATED,
    UNKNOWN
}
