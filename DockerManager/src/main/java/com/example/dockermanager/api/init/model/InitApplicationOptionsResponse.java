package com.example.dockermanager.api.init.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InitApplicationOptionsResponse {
    private List<String> databaseTypes;
    private List<String> volumeTypes;
    private List<String> performanceTestTypes;
}


