package com.example.dockermanager.domain.service.performanceTests;

import com.example.dockermanager.domain.model.performanceTests.PerformanceTestRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class PerformanceTestsResultsService {

    private static final Pattern SAFE_RUN_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern SAFE_ASSET_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._/-]+$");
    private static final Pattern HEAD_TAG_PATTERN = Pattern.compile("<head[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Duration RUNS_SYNC_INTERVAL = Duration.ofSeconds(10);

    private final Path resultsDirectory;
    private final PerformanceTestsVolumeService performanceTestsVolumeService;

    public PerformanceTestsResultsService(@Value("${performance-tests.results-directory:/opt/gatling/results}") String resultsDirectory,
                                          PerformanceTestsVolumeService performanceTestsVolumeService) {
        this.resultsDirectory = Path.of(resultsDirectory).toAbsolutePath().normalize();
        this.performanceTestsVolumeService = performanceTestsVolumeService;
    }

    public List<PerformanceTestRun> listAvailableRuns() {
        performanceTestsVolumeService.syncResultsFromVolumeIfStale(resultsDirectory, RUNS_SYNC_INTERVAL);

        if (!Files.isDirectory(resultsDirectory)) {
            return List.of();
        }

        try (Stream<Path> children = Files.list(resultsDirectory)) {
            return children
                    .filter(Files::isDirectory)
                    .map(this::mapRunDirectory)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(PerformanceTestRun::getStartedAt).reversed())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public String readReportHtmlWithBaseHref(String runId, String baseHref) {
        Path runDirectory = resolveRunDirectoryWithOnDemandSync(runId);
        Path indexPath = runDirectory.resolve("index.html");
        if (!Files.isRegularFile(indexPath)) {
            // Sync on-demand only when report file is missing locally.
            performanceTestsVolumeService.syncResultsFromVolume(resultsDirectory);
            indexPath = runDirectory.resolve("index.html");
        }

        if (!Files.isRegularFile(indexPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found for run: " + runId);
        }

        try {
            String html = Files.readString(indexPath, StandardCharsets.UTF_8);
            return injectBaseHref(html, baseHref);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read report for run: " + runId, e);
        }
    }

    public byte[] readReportAsset(String runId, String assetPath) {
        String normalizedAssetPath = normalizeAndValidateAssetPath(assetPath);
        Path runDirectory = resolveRunDirectoryWithOnDemandSync(runId);
        Path filePath = runDirectory.resolve(normalizedAssetPath).normalize();

        if (!Files.isRegularFile(filePath)) {
            // Sync on-demand only when requested asset is missing locally.
            performanceTestsVolumeService.syncResultsFromVolume(resultsDirectory);
            filePath = runDirectory.resolve(normalizedAssetPath).normalize();
        }

        if (!filePath.startsWith(runDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report asset path");
        }

        if (!Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report asset not found");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read report asset", e);
        }
    }

    public MediaType resolveMediaType(String runId, String assetPath) {
        String normalizedAssetPath = normalizeAndValidateAssetPath(assetPath);
        Path runDirectory = resolveRunDirectoryWithOnDemandSync(runId);
        Path filePath = runDirectory.resolve(normalizedAssetPath).normalize();

        if (!filePath.startsWith(runDirectory) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report asset not found");
        }

        MediaType mappedType = resolveKnownMediaType(normalizedAssetPath);
        if (mappedType != null) {
            return mappedType;
        }

        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null && !contentType.isBlank()) {
                return MediaType.parseMediaType(contentType);
            }
            return MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private MediaType resolveKnownMediaType(String assetPath) {
        String lowerCasePath = assetPath.toLowerCase(Locale.ROOT);
        if (lowerCasePath.endsWith(".css")) {
            return MediaType.parseMediaType("text/css");
        }
        if (lowerCasePath.endsWith(".js") || lowerCasePath.endsWith(".mjs")) {
            return MediaType.parseMediaType("application/javascript");
        }
        if (lowerCasePath.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        }
        if (lowerCasePath.endsWith(".json")) {
            return MediaType.APPLICATION_JSON;
        }
        if (lowerCasePath.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        }
        if (lowerCasePath.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lowerCasePath.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lowerCasePath.endsWith(".txt") || lowerCasePath.endsWith(".log")) {
            return MediaType.TEXT_PLAIN;
        }
        if (lowerCasePath.endsWith(".ico")) {
            return MediaType.parseMediaType("image/x-icon");
        }
        if (lowerCasePath.endsWith(".map")) {
            return MediaType.APPLICATION_JSON;
        }
        return null;
    }

    private Optional<PerformanceTestRun> mapRunDirectory(Path runDirectory) {
        Path indexPath = runDirectory.resolve("index.html");
        if (!Files.isRegularFile(indexPath)) {
            return Optional.empty();
        }

        String runId = runDirectory.getFileName().toString();
        Instant startedAt = readLastModifiedTime(runDirectory);

        return Optional.of(PerformanceTestRun.builder()
                .runId(runId)
                .startedAt(startedAt)
                .build());
    }

    private Instant readLastModifiedTime(Path runDirectory) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(runDirectory);
            return fileTime.toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    private Path resolveRunDirectoryWithOnDemandSync(String runId) {
        validateRunId(runId);

        Path runDirectory = resultsDirectory.resolve(runId).normalize();
        if (!runDirectory.startsWith(resultsDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found: " + runId);
        }

        if (!Files.isDirectory(runDirectory)) {
            performanceTestsVolumeService.syncResultsFromVolume(resultsDirectory);
        }

        if (!Files.isDirectory(runDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found: " + runId);
        }

        return runDirectory;
    }

    private void validateRunId(String runId) {
        if (runId == null || runId.isBlank() || !SAFE_RUN_ID_PATTERN.matcher(runId).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid run id");
        }
    }

    private String normalizeAndValidateAssetPath(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report asset path");
        }

        String normalizedPath = assetPath.replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        if (!SAFE_ASSET_PATH_PATTERN.matcher(normalizedPath).matches()
                || normalizedPath.contains("..")
                || normalizedPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report asset path");
        }

        return normalizedPath;
    }

    private String injectBaseHref(String html, String baseHref) {
        String normalizedBaseHref = baseHref.endsWith("/") ? baseHref : baseHref + "/";
        String baseTag = "<base href=\"" + normalizedBaseHref + "\">";

        String lowerCaseHtml = html.toLowerCase(Locale.ROOT);
        if (lowerCaseHtml.contains("<base ")) {
            return html;
        }

        var matcher = HEAD_TAG_PATTERN.matcher(html);
        if (matcher.find()) {
            int insertionPoint = matcher.end();
            return html.substring(0, insertionPoint) + baseTag + html.substring(insertionPoint);
        }

        return baseTag + html;
    }
}
