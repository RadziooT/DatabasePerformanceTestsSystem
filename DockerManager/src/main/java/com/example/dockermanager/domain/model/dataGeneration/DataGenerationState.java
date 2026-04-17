package com.example.dockermanager.domain.model.dataGeneration;

public enum DataGenerationState {
    IDLE,
    RUNNING,
    SUCCEEDED,
    FAILED;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED;
    }
}
