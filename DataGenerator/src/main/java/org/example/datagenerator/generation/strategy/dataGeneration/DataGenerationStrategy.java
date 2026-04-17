package org.example.datagenerator.generation.strategy.dataGeneration;

import org.example.datagenerator.model.DataGenerationRequest;

public interface DataGenerationStrategy {
    void generateData(DataGenerationRequest request);
}
