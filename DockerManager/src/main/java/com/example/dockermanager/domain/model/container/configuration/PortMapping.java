package com.example.dockermanager.domain.model.container.configuration;

public record PortMapping(int hostPort, int containerPort) {
}
