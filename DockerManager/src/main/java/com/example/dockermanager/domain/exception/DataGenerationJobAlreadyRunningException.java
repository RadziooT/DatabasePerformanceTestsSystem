package com.example.dockermanager.domain.exception;

public class DataGenerationJobAlreadyRunningException extends RuntimeException {
    public DataGenerationJobAlreadyRunningException(String message) {
        super(message);
    }
}
