package org.example.datagenerator.generation.strategy.dataGeneration;

@FunctionalInterface
public interface ProgressReporter {
    void report(int progressPercent, String step, String message);
}
