# DatabasePerformanceTestsSystem

[![Java](https://img.shields.io/badge/Java-21-red?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular)](https://angular.dev/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)](https://www.docker.com/)
[![PowerShell](https://img.shields.io/badge/PowerShell-7+-5391FE?logo=powershell)](https://learn.microsoft.com/powershell/)

## Overview

`DatabasePerformanceTestsSystem` is a test platform for measuring Spring Boot application response times under load while using different database engines.

The repository combines an Angular control UI, a Docker-based orchestration backend, a data generator, a mock business application, and performance test simulations. This root README provides architecture-level orientation, while module-specific details are documented in the component READMEs.

## Architecture

For detailed documentation, refer to the component-level READMEs:

- [DockerManagementUI/README.md](DockerManagementUI/README.md) - Angular control panel for environment management and test execution.
- [DockerManager/README.md](DockerManager/README.md) - Spring Boot orchestration backend that manages containers, volumes, and workflows.
- [PerformanceTests/README.md](PerformanceTests/README.md) - Gatling-based load test project.
- [DataGenerator/README.md](DataGenerator/README.md) - Spring Boot service that populates the database with TPC-C-style data.
- [MockApp/README.md](MockApp/README.md) - Spring Boot application used as the system under test.

## How to run

The repository includes startup scripts for local development:

- `setup-environment.bat` - Windows entry point.
- `setup-environment.sh` - Shell entry point for Unix-like environments.

Both scripts run the setup flow defined in `setup-environment.ps1`, which requires **PowerShell 7 (`pwsh`)**.

### Initialization flow

The setup process performs the following steps:

1. Verifies the local toolchain:
   - `npm` version `>= 9.8.0`
   - `node` version `>= 20`
   - `java` version `21`
   - `maven` version `>= 3.9.0`
   - Docker daemon availability
2. Validates the expected repository folder structure.
3. Pulls required base images:
   - `maven:3.9.9-eclipse-temurin-21-alpine`
   - `mysql:8.3.0`
   - `postgres:16.2-alpine`
   - `gvenzl/oracle-free:latest`
   - `mcr.microsoft.com/mssql/server:2022-latest`
4. Builds local project images:
   - `docker-manager-performance-tests`
   - `docker-manager-data-generator`
   - `docker-manager-mock-app`
5. Sets DockerManager-related environment variables.
6. Installs and builds the frontend application.
7. Starts frontend and backend in separate PowerShell sessions.

## Environment variables

The `setup-environment.ps1` script sets the following values during initialization:

| Variable | Value |
| --- | --- |
| `DOCKER_HOST` | `tcp://localhost:2375` |
| `DOCKER_MANAGER_DB_NAME` | `testDb` |
| `DOCKER_MANAGER_DB_PASSWORD` | `Password123!` |
| `DOCKER_MANAGER_DB_USERNAME` | `sa` |

These values are intended for the local development workflow launched by the setup scripts.

## How to use

After startup is complete, open the UI at:

**http://127.0.0.1:4200**

Recommended workflow:

1. Review available options in the UI.
2. Start or verify the target environment.
3. Run data generation if a preloaded dataset is required.
4. Execute the selected performance simulation.
5. Inspect generated reports and compare results.

## Project layout

```text
DatabasePerformanceTestsSystem/
|- DockerManagementUI/      # Angular frontend
|- DockerManager/           # Spring Boot orchestration backend
|- DataGenerator/           # Database population service
|- MockApp/                 # Spring Boot system under test
|- PerformanceTests/        # Gatling load tests
|- setup-environment.ps1    # PowerShell 7 setup flow
|- setup-environment.bat    # Windows launcher
\- setup-environment.sh     # Shell launcher
```

## Notes

- Operational and implementation details are documented in module-specific READMEs.
- The setup flow is designed for local, Docker-based development and test execution.
