package org.example.datagenerator.model;

import org.example.datagenerator.generation.strategy.dataGeneration.ProgressReporter;

public record DataGenerationRequest(VolumeSize volumeType, ProgressReporter progressReporter) {
}
