package com.example.dockermanager.api.performanceTests;

import com.example.dockermanager.api.performanceTests.mapper.PerformanceTestsResponseMapper;
import com.example.dockermanager.api.performanceTests.model.PerformanceTestRunRequest;
import com.example.dockermanager.api.performanceTests.model.PerformanceTestRunResponse;
import com.example.dockermanager.domain.service.environmentStartup.EnvironmentStartupService;
import com.example.dockermanager.domain.service.performanceTests.PerformanceTestsResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/performance-tests")
@RequiredArgsConstructor
public class PerformanceTestsController {

    private final EnvironmentStartupService environmentStartupService;
    private final PerformanceTestsResultsService performanceTestsResultsService;

    @PostMapping("/run")
    public ResponseEntity<?> startPerformanceTests(@RequestBody PerformanceTestRunRequest request) {
        try {
            if (request == null || request.getSimulationType() == null) {
                return ResponseEntity.badRequest().body("simulationType is required");
            }
            if (request.getDatabaseType() == null) {
                return ResponseEntity.badRequest().body("databaseType is required");
            }
            environmentStartupService.startPerformanceTestsSequence(
                    request.getSimulationType(),
                    request.getVolumeSize(),
                    request.getDatabaseType()
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/previous-runs")
    public ResponseEntity<List<PerformanceTestRunResponse>> getPerformanceTestRuns() {
        return ResponseEntity.ok(performanceTestsResultsService.listAvailableRuns().stream()
                .map(PerformanceTestsResponseMapper::toPerformanceTestRunResponse)
                .toList());
    }

    @GetMapping(value = "/runs/{runId}/report", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getPerformanceTestReport(@PathVariable String runId) {
        String baseHref = "/api/performance-tests/static/" + runId + "/";
        String html = performanceTestsResultsService.readReportHtmlWithBaseHref(runId, baseHref);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/runs/{runId}/files/{*assetPath}")
    public ResponseEntity<Resource> getPerformanceTestReportAsset(@PathVariable String runId,
                                                                  @PathVariable String assetPath) {
        byte[] content = performanceTestsResultsService.readReportAsset(runId, assetPath);
        MediaType mediaType = performanceTestsResultsService.resolveMediaType(runId, assetPath);
        String normalizedAssetPath = assetPath.toLowerCase(Locale.ROOT);

        CacheControl cacheControl = CacheControl.noCache();
        if (normalizedAssetPath.endsWith(".css")
                || normalizedAssetPath.endsWith(".js")
                || normalizedAssetPath.endsWith(".mjs")
                || normalizedAssetPath.endsWith(".svg")) {
            cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(cacheControl)
                .body(new ByteArrayResource(content));
    }
}
