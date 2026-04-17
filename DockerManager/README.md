# DockerManager

## Overview

**DockerManager** is a Spring Boot application that orchestrates Docker containers for database response time testing.
It acts as the **core bridge between UI/CLI and containerized runtime**, managing the complete lifecycle of test
environments, performance simulations, and data generation workflows.

### Architecture Role

```
┌─────────────┐
│   Frontend  │  (WebUI / CLI)
└──────┬──────┘
       │ REST API (Port 8000)
       ▼
┌─────────────────────────────────────────────┐
│          DockerManager                      │
├─────────────────────────────────────────────┤
│ • Container Lifecycle Management            │
│ • Configuration Loading & Placeholder Res.  │
│ • Docker Network & Volume Management        │
│ • Orchestration Flows (ENV, Tests, DataGen) │
│ • Performance Test Result Handling          │
│ • Data Generation Job Tracking & Callbacks  │
└──────┬──────────────────────────────────────┘
       │ Docker API
       ▼
┌──────────────────────────────────────────────┐
│        Docker Engine (docker-manager-network)│
├──────────────────────────────────────────────┤
│ ┌──────────┐ ┌─────────┐ ┌──────────┐       │
│ │PostgreSQL│ │ MySQL   │ │ Oracle   │ ...   │
│ │ /SqlSrvr │ │ /Mock   │ │ /Gatling │       │
│ │          │ │ /DataGen│ │          │       │
│ └──────────┘ └─────────┘ └──────────┘       │
│                (bridge network)              │
└──────────────────────────────────────────────┘
```

---

## Project Structure

```
src/main/java/com/example/dockermanager/
├── api/                          # REST Controllers & DTOs
│   ├── init/                      # Available options endpoint
│   ├── environment/               # Environment start/init
│   ├── container/                 # Container lifecycle (start/stop/delete)
│   ├── volume/                    # Volume operations
│   ├── performanceTests/          # Performance test runs & reports
│   └── dataGeneration/            # Data generation start/status/callback
│
├── domain/
│   ├── model/                     # Domain entities
│   │   ├── container/             # ContainerType, ContainerDefinition, ContainerSummary
│   │   ├── volume/                # VolumeType, VolumeSummary, VolumeStatus
│   │   ├── environment/           # DatabaseType, VolumeSize, RunEnvironmentConfig
│   │   ├── dataGeneration/        # DataGenerationJob, DataGenerationState, DataGenerationStatus
│   │   └── performanceTests/      # PerformanceTestRun
│   │
│   ├── service/                   # Business logic
│   │   ├── environmentStartup/    # Environment setup orchestration
│   │   ├── container/             # Docker container ops, configurators
│   │   ├── volume/                # Docker volume ops
│   │   ├── dataGeneration/        # Data generation workflow
│   │   ├── performanceTests/      # Gatling result handling & volume sync
│   │   └── util/                  # Mapping (DatabaseType → ContainerType)
│   │
│   └── exception/                 # Domain-specific exceptions
│
├── configuration/
│   ├── DockerConfig.java          # Docker client setup (DOCKER_HOST)
│   └── WebConfiguration.java       # CORS, resource handlers
│
└── resources/
    ├── application.yml            # Main config (imports container configs)
    └── config/containers/         # Per-container definitions
        ├── mysql.yml
        ├── postgresql.yml
        ├── oracle.yml
        ├── sqlserver.yml
        ├── mock-app.yml
        ├── data-generator.yml
        └── gatling.yml
```

---

## Core Concepts

### Supported Container Types

Container types are defined in `ContainerType` enum:

| Type               | Container Name                     | Network Alias       | Purpose                                |
|--------------------|------------------------------------|---------------------|----------------------------------------|
| **POSTGRESQL**     | `docker-manager-postgres`          | `postgres`          | PostgreSQL database                    |
| **MYSQL**          | `docker-manager-mysql`             | `mysql`             | MySQL database                         |
| **ORACLE**         | `docker-manager-oracle`            | `oracle`            | Oracle database                        |
| **SQLSERVER**      | `docker-manager-sqlserver`         | `sqlserver`         | MS SQL Server                          |
| **MOCK_APP**       | `docker-manager-mock-app`          | `mock-app`          | HTTP application server (targets a DB) |
| **DATA_GENERATOR** | `docker-manager-data-generator`    | `data-generator`    | Populates database via callbacks       |
| **GATLING**        | `docker-manager-performance-tests` | `performance-tests` | Gatling load test runner               |

### Volume Types

Volumes are organized by database type and size tier:

- **POSTGRESQL_SMALL/MEDIUM/LARGE** → `docker-manager-postgres-{small|medium|large}`
- **MYSQL_SMALL/MEDIUM/LARGE** → `docker-manager-mysql-{small|medium|large}`
- **ORACLE_SMALL/MEDIUM/LARGE** → `docker-manager-oracle-{small|medium|large}`
- **SQLSERVER_SMALL/MEDIUM/LARGE** → `docker-manager-sqlserver-{small|medium|large}`
- **GATLING** → `gatling-results-local` (stores Gatling reports)

### Database Types

User-facing enum mapping to ContainerType and VolumeSize:

- `MYSQL`, `POSTGRESQL`, `ORACLE`, `SQLSERVER`

### Volume Sizes

User-facing enum:

- `SMALL`, `MEDIUM`, `LARGE`

---

## Configuration System

### Configuration Loading Path

```
src/main/resources/application.yml
  ├─ Imports 7 container config files (Spring config.import)
  │  ├─ config/containers/mysql.yml
  │  ├─ config/containers/postgres.yml
  │  ├─ config/containers/oracle.yml
  │  ├─ config/containers/sqlserver.yml
  │  ├─ config/containers/mock-app.yml
  │  ├─ config/containers/data-generator.yml
  │  └─ config/containers/gatling.yml
  │
  ├─ Defines shared database credentials (placeholders)
  │  └─ docker.shared-db.{username, password, name}
  │
  ├─ Defines performance test base URL
  │  └─ docker.performance-tests.base-url
  │
  └─ Defines data generation settings
     └─ data-generation.{callback-url, callback-token}
```

### Placeholder Resolution

`DockerContainersConfigProperties` (with `@ConfigurationProperties(prefix = "docker")`) loads YAML into enums:

**Example from mysql.yml:**

```yaml
docker:
  containers:
    MYSQL:
      image: "mysql:8.3.0"
      containerName: "docker-manager-mysql"
      environment:
        MYSQL_USER: "${docker.shared-db.username}"        # Placeholder
        MYSQL_PASSWORD: "${docker.shared-db.password}"    # Resolved at boot
        MYSQL_DATABASE: "${docker.shared-db.name}"
```

**Resolution Flow:**

1. `DockerContainersConfigProperties` loads raw YAML → `Map<ContainerType, ContainerDefinition>`
2. `ContainerDefinitionMapper` validates required fields (containerName, image) per type
3. `ContainerDefinitionPlaceholderResolver` calls `environment.resolveRequiredPlaceholders()` on all values
4. `ContainerDefinitionService` caches resolved definitions via `@PostConstruct`

**Validation:**

- Missing required env vars (DOCKER_MANAGER_DB_*) cause **fail-fast** startup error
- Unresolved placeholders (e.g., typos) throw `IllegalArgumentException` during boot resolution

### Required Environment Variables

Must be set before startup (fail-fast validation):

- `DOCKER_MANAGER_DB_USERNAME` - username for all database containers (maps to `docker.shared-db.username`)
  > **⚠️ VERY IMPORTANT WARNING:** `DOCKER_MANAGER_DB_USERNAME` **has to be "sa"** for SQL Server to work properly.
  > **NEVER USE DIFFERENT VALUE.**
  > (Other containers will automatically use this same username natively).
- `DOCKER_MANAGER_DB_PASSWORD` - password for all database containers (maps to `docker.shared-db.password`)
- `DOCKER_MANAGER_DB_NAME` - database name created in all containers (maps to `docker.shared-db.name`)

### Optional Environment Variables

| Variable                         | Default                                                         | Usage                                                                                |
|----------------------------------|-----------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `DOCKER_HOST`                    | Required                                                        | Docker daemon socket (e.g., `unix:///var/run/docker.sock` or `tcp://localhost:2375`) |
| `PERFORMANCE_TESTS_BASE_URL`     | `http://host.docker.internal:8080`                              | Base URL passed to Gatling tests via `SIMULATION_CLASS` injection                    |
| `GATLING_RESULTS_DIR`            | `./target/gatling`                                              | Local directory where Gatling reports are synced from Docker volume                  |
| `DATA_GENERATOR_IMAGE`           | `docker-manager-data-generator:latest`                          | Data generator container image name                                                  |
| `DATA_GENERATION_CALLBACK_URL`   | `http://host.docker.internal:8000/api/data-generation/callback` | URL data generator calls to report progress                                          |
| `DATA_GENERATION_CALLBACK_TOKEN` | `docker-manager-dev-token`                                      | Security token for callback endpoint validation                                      |

---

## Container Management

### Container Configuration Hierarchy

Each container type implements `ContainerConfiguration` which applies:

1. **Common settings** (name, image, ports, environment, labels, volumes)
2. **Host config** (memory, restart policy, CPU, network)
3. **Type-specific hooks** (health checks, custom command args)

**Base Template (ContainerConfiguration.applyContainerConfiguration):**

```
1. Set container name and hostname
2. Apply host config:
   - Port mappings
   - Memory limit
   - Restart policy
   - Shared network (docker-manager-network bridge)
   - Custom host config per type
3. Apply environment variables:
   - From definition + runtime overrides
4. Apply labels (for container tracking)
5. Mount volumes:
   - Static volumes from definition
   - Dynamic volume (if applicable)
6. Set health check
```

### Per-Type Configuration Details

#### Database Containers (PostgreSQL, MySQL, Oracle, SQL Server)

All extend `DatabaseContainerConfiguration`:

- **Shared host resources:** 2 GiB memory, 2 CPU cores
- **Shared health-check policy:** interval 10s, timeout 10s, retries 5
- **Restart policy:** `unless-stopped`
- **Volume target:** Database-specific mount point
- **Health check helper:** database-specific command + optional Oracle `startPeriod`
- **Oracle-specific:** 1 GiB shared memory (`shmSize`) and `startPeriod` of 90s

The database configurators use a shared helper in `DatabaseContainerConfiguration` to build `HealthCheck` objects.
Each database only provides its probe command, while interval, timeout, and retries stay identical for all engines.

Oracle keeps a different `shmSize` because the Oracle image requires larger shared memory to start correctly in Docker.
This is a startup compatibility requirement, not a benchmark advantage.

**PostgreSQL (PostgresqlContainerConfiguration):**

```java
private Optional<String> volumeTargetPath() {
    return Optional.of("/var/lib/postgresql/data");
}

private void applyCustomContainerConfig(...) {
    createContainerCmd.withHealthcheck(buildHealthCheck(
            "pg_isready -d $POSTGRES_DB -U $POSTGRES_USER || exit 1"
    ));
}
```

**MySQL (MysqlContainerConfiguration):**

```java
private Optional<String> volumeTargetPath() {
    return Optional.of("/var/lib/mysql");
}

// Also enables LOAD DATA LOCAL INFILE for data generation
private void applyCustomContainerConfig(...) {
    createContainerCmd.withHealthcheck(buildHealthCheck(
            "mysqladmin ping -h 127.0.0.1 -u root -p$MYSQL_ROOT_PASSWORD || exit 1"
    ));
    createContainerCmd.withCmd("--local-infile=1");
}
```

**SQL Server (SqlServerContainerConfigurationService):**

```java
private Optional<String> volumeTargetPath() {
    return Optional.of("/var/opt/mssql");
}

private void applyCustomContainerConfig(...) {
    createContainerCmd.withHealthcheck(buildHealthCheck(
            "/opt/mssql-tools18/bin/sqlcmd -S localhost -C -U sa -P ${MSSQL_SA_PASSWORD:-$SA_PASSWORD} -Q 'SELECT 1' || exit 1"
    ));
}
```

**Oracle (OracleContainerConfigurationService):**

```java
private Optional<String> volumeTargetPath() {
    return Optional.of("/opt/oracle/oradata");
}

private void applyAdditionalHostConfig(HostConfig hostConfig) {
    hostConfig.withShmSize(1_073_741_824L);  // 1 GB shared memory
}

private void applyCustomContainerConfig(...) {
    createContainerCmd.withHealthcheck(buildHealthCheck(
            "/opt/oracle/healthcheck.sh || exit 1",
            ORACLE_HEALTHCHECK_START_PERIOD_NANOS
    ));
}
```

#### Mock App Container

- **Memory:** 512 MB
- **Restart policy:** Restart on failure (max 3 retries)
- **Runtime environment override:** `SPRING_PROFILES_ACTIVE` set to database type (lowercase)

```java

@Override
private Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput config) {
    return Map.of("SPRING_PROFILES_ACTIVE", config.getDatabaseType().name().toLowerCase());
    // e.g., "SPRING_PROFILES_ACTIVE=mysql" when targeting MySQL
}
```

#### Gatling Container

- **Memory:** 1 GB
- **CPU:** 2 cores
- **Volume target:** `/app/target/gatling` (mounted to `gatling-results-local` volume)
- **Runtime environment override:** `SIMULATION_CLASS` from `PerformanceSimulationType` enum

```java

@Override
private Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput config) {
    if (config.getPerformanceTestSimulationType() == null) {
        return Map.of();
    }
    return Map.of("SIMULATION_CLASS", config.getPerformanceTestSimulationType().getSimulationClass());
}
```

#### Data Generator Container

- **Memory:** 1 GB
- **CPU:** 1 core
- **Runtime environment overrides:** Full database connection config injected by
  `DataGeneratorContainerConfigurationService`:

```java

@Override
private Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput config) {
    DataGenerationContainerParams request = config.getDataGenerationContainerParams();
    if (request == null) {
        return Map.of();
    }

    var dbProps = resolveDatabaseConnection(request.databaseType());

    Map<String, String> env = new LinkedHashMap<>();
    env.put("GENERATOR_JOB_ID", request.jobId());
    env.put("GENERATOR_DB_TYPE", request.databaseType().name().toUpperCase());
    env.put("GENERATOR_VOLUME_TYPE", request.volumeType().name().toUpperCase());
    env.put("GENERATOR_DB_PORT", String.valueOf(dbProps.port));
    env.put("GENERATOR_DB_NAME", dbProps.databaseName);
    env.put("GENERATOR_DB_USER", dbProps.user);
    env.put("GENERATOR_DB_PASSWORD", dbProps.password);
    env.put("GENERATOR_CALLBACK_URL", callbackUrl);
    env.put("GENERATOR_CALLBACK_TOKEN", callbackToken);

    return env;
}
```

Connection details are resolved from the running database container's definition and environment variables.

### Shared Docker Network

All containers are connected to a shared **bridge network** named `docker-manager-network` via `DockerNetworkService`:

```java

@Service
public class DockerNetworkService {
    private static final String SHARED_NETWORK_NAME = "docker-manager-network";

    private void ensureSharedNetworkExists() {
        try {
            dockerClient.inspectNetworkCmd().withNetworkId(SHARED_NETWORK_NAME).exec();
        } catch (NotFoundException e) {
            dockerClient.createNetworkCmd()
                    .withName(SHARED_NETWORK_NAME)
                    .withDriver("bridge")
                    .exec();
        }
    }

    public void withCustomSharedNetwork(HostConfig hostConfig) {
        ensureSharedNetworkExists();
        hostConfig.withNetworkMode(SHARED_NETWORK_NAME);
    }
}
```

This enables inter-container DNS resolution by name (e.g., `docker-manager-mysql:3306`).

### Container Lifecycle

**ContainerService** manages all container operations:

**Creation Flow:**

```
ContainerService.createContainer(type, volumeType, ...)
  ↓
1. Get ContainerDefinition from cached definitions
2. Check image exists (fail if not)
3. Check/create volume if needed (via VolumeService)
4. Build CreateContainerCmd:
   - Resolve ContainerConfiguration via factory
   - Apply configuration (ports, env, labels, mounts, network)
   - Mount volumes (from definition + dynamic volume)
   - Set labels: {"app": "docker-manager", "managed-by": "docker-manager"}
  ↓
5. Execute via DockerClient
```

**Startup & Health Check:**

```
ContainerService.startContainer(type)
  ↓
1. Find container by name
2. Execute startContainerCmd via DockerClient
3. (Optional) Wait for ready:
   ContainerService.waitForContainerReady(type, Duration.ofSeconds(90))
   - Poll every 2 seconds
   - Check: running state = true AND (healthStatus = "healthy" OR no health check)
   - Fail if: health status = "unhealthy" OR timeout exceeded
```

**State Management:**

- **Listing:** Only managed containers (label filter: {"app": "docker-manager"})
- **Available Actions:**
    - `RUNNING` → can `STOP`
    - `CREATED/PAUSED/STOPPED/EXITED` → can `DELETE`

---

## Volume Management

### Volume Service

`VolumeService` handles creation, listing, and deletion:

```java
public List<VolumeSummary> listVolumes() {
    // Maps all VolumeType enum values to actual Docker volumes
    // Returns CREATED if exists, NOT_CREATED if missing
}

public void createVolume(VolumeType volumeType) {
    dockerClient.createVolumeCmd().withName(volumeType.getVolumeName()).exec();
}

public void deleteAllVolumes() {
    // Deletes all managed volumes
}

public void deleteVolume(VolumeType volumeType) {
    dockerClient.removeVolumeCmd(volumeType.getVolumeName()).exec();
}
```

### Volume Utility

`VolumeUtil` provides existence checks:

```java
public boolean isVolumePresent(VolumeType volumeType) {
    try {
        dockerClient.inspectVolumeCmd(volumeType.getVolumeName()).exec();
        return true;
    } catch (NotFoundException e) {
        return false;
    }
}
```

---

## Orchestration Flows

### Environment Startup Flow

**Endpoint:** `POST /api/environment/start`

**Request:**

```json
{
  "databaseType": "POSTGRESQL",
  "databaseVolumeType": "MEDIUM",
  "shutdownContainers": true,
  "removeContainers": false,
  "deleteVolumes": false
}
```

**Implementation (EnvironmentStartupService.startEnvironment):**

```
1. Conditionally shutdown all running containers (if shutdownContainers=true)
2. Conditionally delete all volumes (if deleteVolumes=true)
3. Conditionally remove all stopped containers (if removeContainers=true)

4. Map DatabaseType → ContainerType via DatabaseResourceMappingService
   Example: POSTGRESQL → ContainerType.POSTGRESQL

5. Map VolumeSize → VolumeType
   Example: POSTGRESQL + MEDIUM → VolumeType.POSTGRESQL_MEDIUM

6. Create database container:
   - ContainerService.createContainer(mappedDbContainerType, mappedVolumeType)
   - ContainerService auto-creates volume if missing
   - DatabaseContainerConfiguration applies health checks

7. Manage MOCK_APP container:
   - Stop if running
   - Remove if created
   - ContainerService.createMockAppContainer(MOCK_APP, dbContainerType)
   - MockAppContainerConfiguration injects SPRING_PROFILES_ACTIVE

8. Start database container:
   - ContainerService.startContainer(mappedDbContainerType)
   - ContainerService.waitForContainerReady(mappedDbContainerType, Duration.ofSeconds(90))

9. Start MOCK_APP container:
   - ContainerService.startContainer(MOCK_APP)
```

**Result:** Environment ready with database and application server on shared network.

### Performance Test Execution Flow

**Endpoint:** `POST /api/performance-tests/run`

**Request:**

```json
{
  "simulationType": "BASIC_SIMULATION",
  "databaseType": "POSTGRES",
  "volumeSize": "SMALL"
}
```

**Implementation (EnvironmentStartupService.startPerformanceTestsSequence):**

```
1. Validate simulationType and databaseType are not null

2. Create performance test params and create GATLING container
   - GatlingContainerConfiguration injects SIMULATION_CLASS, DATASET_SIZE, DATABASE_TYPE env vars
   - ContainerService mounts gatling-results-local volume → /app/target/gatling
   - Volume created if missing

3. ContainerService.startContainer(GATLING)

4. Gatling executes inside container:
   - Reads SIMULATION_CLASS from environment
   - Reads DATABASE_TYPE from environment
   - Reads BASE_URL (docker.performance-tests.base-url) from environment
   - Makes HTTP requests to mock-app via docker-manager-network
   - Writes results to /app/target/gatling/<runId>/

5. (Async) PerformanceTestsVolumeService syncs results:
   - Uses docker create + docker cp workaround for Windows volume access
   - Copies from volume to local GATLING_RESULTS_DIR
   - Syncs on first request for results, then at 10-second intervals
```

**Results Handling:**

- `PerformanceTestsResultsService.listAvailableRuns()` scans local directory
- `PerformanceTestsResultsService.readReportHtmlWithBaseHref()` loads HTML and injects relative `<base href>` for assets
- Asset serving via resource handler configured in `WebConfiguration`

---

## Data Generation Workflow

### Start Data Generation

**Endpoint:** `POST /api/data-generation/start`

**Request:**

```json
{
  "databaseType": "MYSQL",
  "volumeType": "LARGE"
}
```

**Prerequisites Check (DataGenerationService.validatePrerequisites):**

- Database container must be running (status = "running")
- Volume must exist
- Volume must be mounted on database container (checked via ContainerMount inspection)

**Workflow (DataGenerationService.startJob):**

```
1. Validate request:
   - databaseType not null
   - volumeType not null

2. Map to ContainerType and VolumeType via DatabaseResourceMappingService

3. Check prerequisites:
   - isContainerRunning(dbContainerType)
   - isVolumePresent(volumeType)
   - isVolumeMounted(dbContainerType, volumeType)
   Throws DataGenerationPrerequisiteException if any fail

4. Generate unique jobId = UUID.randomUUID().toString()

5. Create DataGenerationJob in memory:
   - jobId: UUID
   - state = RUNNING
   - progressPercent = 0
   - startedAt = Instant.now()
   Stored in synchronized lock in DataGenerationService

6. Stop/remove any existing DATA_GENERATOR container

7. Create DATA_GENERATOR container:
   - ContainerService.createContainer(DATA_GENERATOR, dataGenerationContainerParams)
   - DataGeneratorContainerConfigurationService resolves database connection:
     • Host: containerType.getDefaultNetworkAlias() (e.g., "mysql")
     • Port: from container definition port mapping
     • Database: from container definition environment (MYSQL_DATABASE, POSTGRES_DB, etc.)
     • User/Password: from container definition environment
   - All connection info injected as GENERATOR_DB_* env vars
   - Callback URL and token injected as GENERATOR_CALLBACK_*

8. Start DATA_GENERATOR container

9. Return DataGenerationStatus with jobId and state=RUNNING
```

**Container Execution (inside docker-manager-data-generator):**

- Reads GENERATOR_* environment variables
- Connects to database at GENERATOR_DB_HOST:GENERATOR_DB_PORT
- Generates data in steps/phases
- Makes HTTP POST calls to GENERATOR_CALLBACK_URL with job progress
- Calls endpoint with header `X-Generator-Token: <token>`

### Data Generation Callback

**Endpoint:** `POST /api/data-generation/callback`

**Headers:** `X-Generator-Token: <token>`

**Request:**

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "state": "RUNNING",
  "progressPercent": 50,
  "currentStep": "Inserting orders",
  "message": "Processing order batch 5"
}
```

**Implementation (DataGenerationService.applyCallback):**

```
1. Validate callback token matches configured token (DATA_GENERATION_CALLBACK_TOKEN)
   Throws SecurityException if mismatch (HTTP 401)

2. Validate jobId is present and not blank
   Throws IllegalArgumentException if invalid (HTTP 400)

3. Acquire synchronized lock on DataGenerationJob state

4. Find DataGenerationJob by jobId in memory

5. If job not found OR jobId mismatch OR job already terminal:
   Return false (HTTP 202 Accepted - ignore stale callback)

6. Update job fields:
   - progressPercent = callback.progressPercent (if not null)
   - currentStep = callback.currentStep (if not null)
   - state = callback.state (if not null)

7. If state is terminal (SUCCEEDED or FAILED):
   - Set progressPercent = 100 if < 100
   - Set finishedAt = Instant.now()

8. Log callback receipt with progress

9. Return success (HTTP 200)
```

### Get Data Generation Status

**Endpoint:** `GET /api/data-generation/status`

**Response:**

```json
{
  "state": "RUNNING",
  "progressPercent": 50,
  "currentStep": "Inserting orders",
  "startedAt": "2026-04-04T10:00:00Z",
  "finishedAt": null
}
```

**Implementation:**

```
1. Acquire synchronized lock
2. If no job active: return IDLE state with 0% progress
3. If job exists: return current job status
4. Release lock
```

---

## API & UI Integration

### CORS Configuration

**File:** `configuration/WebConfiguration.java`

Enabled for frontend development servers:

- `http://localhost:4200` (Angular)
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (React/Node)

**Allowed:**

- Methods: `GET`, `HEAD`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- Headers: `*`
- Credentials: `true`

### Resource Handlers

Gatling reports served as static resources:

```java
registry.addResourceHandler("/api/performance-tests/static/**")
        .

addResourceLocations(GATLING_RESULTS_DIR)
        .

setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES)
        .

cachePublic());
```

### API Endpoints Summary

#### Initialization & Configuration

- `GET /api/init` → `InitApplicationOptionsResponse`
    - Returns available databaseTypes, volumeTypes, performanceTestTypes (enum names as strings)

#### Environment Management

- `POST /api/environment/start` → runs `EnvironmentStartupService`
    - Takes `RunEnvironmentConfig` (databaseType, volumeType, cleanup flags)

#### Container Management

- `GET /api/containers` → `List<ContainerResponse>`
    - Lists all managed containers with state and available actions
- `POST /api/containers/stop` → stop all containers
- `POST /api/containers/stop/{containerType}` → stop specific container
- `DELETE /api/containers/delete/{containerType}` → delete specific container

#### Volume Management

- `GET /api/volumes` → `List<VolumeResponse>`
    - Lists all volumes (enum values + actual Docker volumes) with status
- `DELETE /api/volumes/delete` → delete all volumes
- `DELETE /api/volumes/{volumeType}` → delete specific volume

#### Performance Tests

- `GET /api/performance-tests/previous-runs` → `List<PerformanceTestRunResponse>`
    - Lists Gatling runs from local directory (newest first)
- `POST /api/performance-tests/run` → start Gatling simulation
    - Takes `PerformanceTestRunRequest` (`simulationType`, `databaseType`, optional `volumeSize`)
- `GET /api/performance-tests/runs/{runId}/report` → HTML report
    - Reads from GATLING_RESULTS_DIR with injected `<base href>`
- `GET /api/performance-tests/runs/{runId}/files/{*assetPath}` → report assets
    - CSS, JS, images with appropriate cache control (10 min for static assets)

#### Data Generation

- `POST /api/data-generation/start` → start data generation job
    - Takes `DataGenerationStartRequest` (databaseType, volumeType)
    - Returns `DataGenerationStatusResponse` with jobId
    - HTTP 202 if accepted, 400 if validation error, 409 if job already running
- `GET /api/data-generation/status` → get current job status
    - Returns `DataGenerationStatusResponse`
- `POST /api/data-generation/callback` → internal callback from data generator
    - With `X-Generator-Token` header for security
    - Takes `DataGenerationCallbackRequest`
    - HTTP 200 if accepted, 202 if ignored (stale), 401 if token invalid, 400 if malformed

### Response Models (JSON)

**ContainerResponse:**

```json
{
  "id": "abc123def456",
  "name": "docker-manager-mysql",
  "image": "mysql:8.3.0",
  "containerType": "MYSQL",
  "state": "RUNNING",
  "volumes": [
    "docker-manager-mysql-medium"
  ],
  "availableActions": [
    "STOP"
  ]
}
```

**VolumeResponse:**

```json
{
  "name": "docker-manager-postgres-large",
  "mountpoint": "/var/lib/docker/volumes/docker-manager-postgres-large/_data",
  "volumeType": "POSTGRESQL_LARGE",
  "status": "CREATED"
}
```

**DataGenerationStatusResponse:**

```json
{
  "state": "RUNNING",
  "progressPercent": 75,
  "currentStep": "Finalizing",
  "startedAt": "2026-04-04T10:00:00Z",
  "finishedAt": null
}
```

**PerformanceTestRunResponse:**

```json
{
  "runId": "basicsimulation-20260404110000000",
  "startedAt": "2026-04-04T11:00:00Z"
}
```

**InitApplicationOptionsResponse:**

```json
{
  "databaseTypes": [
    "MYSQL",
    "POSTGRESQL",
    "ORACLE",
    "SQLSERVER"
  ],
  "volumeTypes": [
    "SMALL",
    "MEDIUM",
    "LARGE"
  ],
  "performanceTestTypes": [
    "BASIC_SIMULATION"
  ]
}
```

---

## Technology Stack

- **Framework:** Spring Boot 4.0.1
- **Java Version:** 21
- **Docker Client:** docker-java 3.7.0 + Apache HttpClient5 5.x
- **Configuration:** Spring Boot `@ConfigurationProperties` with YAML (Jackson YAML support)
- **Build:** Maven (Spring Boot Maven Plugin)
- **Additional:** Lombok, Jackson databind

---

## Running the Application

### Prerequisites

1. **Docker daemon running** with `DOCKER_HOST` environment variable set
2. **Required environment variables** for database credentials
3. **Container images** must be available:
    - Base databases: `mysql:8.3.0`, `postgres:16.2-alpine`, `gvenzl/oracle-free:latest`,
      `mcr.microsoft.com/mssql/server:2022-latest`
    - Custom images: `docker-manager-mock-app:latest`, `docker-manager-data-generator:latest`,
      `docker-manager-performance-tests:latest`

### Startup

```bash
export DOCKER_HOST=unix:///var/run/docker.sock
export DOCKER_MANAGER_DB_USERNAME=testuser
export DOCKER_MANAGER_DB_PASSWORD=testpass123
export DOCKER_MANAGER_DB_NAME=testdb

# Optional
export PERFORMANCE_TESTS_BASE_URL=http://host.docker.internal:8080
export GATLING_RESULTS_DIR=./target/gatling
export DATA_GENERATION_CALLBACK_URL=http://host.docker.internal:8000/api/data-generation/callback

mvn spring-boot:run
```

Application starts on **port 8000**.

### Expected Logs

```
Starting DockerManagerApplication...
Resolving container definitions...
✓ docker.containers.POSTGRESQL configuration loaded
✓ docker.containers.MYSQL configuration loaded
... (all container types)
Resolving placeholders in container definitions...
✓ Placeholder resolution successful
Creating shared network: docker-manager-network
DockerManagerApplication started in XXXms
```

---

## Implementation Notes

### Thread Safety

- Data generation job state is guarded by `Object lock` in `DataGenerationService`
- Prevents concurrent job submissions
- Ensures callback atomicity (atomic state updates)

### Error Handling

- **Fail-fast validation:** Missing required env vars cause immediate startup failure with clear error message
- **Placeholder resolution:** Unresolved placeholders throw exception during boot
- **Container prerequisites:** Data generation validates DB running, volume exists, and mount present before starting
- **Health checks:** Wait for container ready with timeout, throws `IllegalStateException` if timeout or unhealthy
  status

### Extensibility

To add a new database type:

1. Add to `DatabaseType` enum
2. Create container config YAML in `src/main/resources/config/containers/`
3. Create `ContainerConfiguration` subclass in `domain/service/container/configurators/`
4. Register as Spring `@Component`
5. Add database-specific mappings to `DataGeneratorContainerConfigurationService.resolveDatabaseConnection()`
6. Update volume type enum if needed (SMALL/MEDIUM/LARGE variants)

---

## References

- **Docker Java API:** https://github.com/docker-java/docker-java
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Gatling:** https://gatling.io/
- **Docker Bridge Networks:** https://docs.docker.com/network/bridge/
