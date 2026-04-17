package com.example.dockermanager.domain.exception;

public class ContainerAlreadyExistsException extends RuntimeException {
    public ContainerAlreadyExistsException(String message) {
        super(message);
    }
}
