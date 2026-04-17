# DockerManagementUI

## Overview

`DockerManagementUI` is an Angular application used to control Docker-based test environments,
trigger backend data generation, and run performance test simulations with report preview.

The UI is designed as an operations dashboard for the DockerManager ecosystem, not as a standalone
testing backend.

## Run locally

Install dependencies and start the development server:

```bash
npm install
npm start
```

_Note: Running `npm install` automatically triggers the generation of the Angular API client from the OpenAPI specification._

Useful additional commands:

```bash
npm run build
npm run test
npm run lint
```

## Folder structure

Main project structure:

```text
DockerManagementUI/
  openapi/
    api-spec.yaml                # Backend OpenAPI contract
  src/
    app/
      api/                       # Generated OpenAPI Angular client (services + models)
      core/
        interceptors/            # HTTP error interceptor
        providers/               # API configuration provider
        services/                # App-level wrappers around generated API services
      layout/
        main-layout.component.*  # Main shell (toolbar + sidenav)
        sidebar/                 # Navigation links
        main-page/
          info/                  # First-start information page
          docker-resources/      # Containers + volumes management
          environment-management/# Environment start + data generation flow
          performance-tests/     # Test run + available runs + HTML report viewer
      shared/components/         # Reusable modals and loading spinner
      store/app.state.ts         # Global options state using Angular signals
    environments/                # Environment configuration files
```

## App functionality

The current UI flow is route-based and lazy-loads feature pages:

- `Info` page (`/info`) describes the intended workflow and key operational notes.
- `Docker Resources` (`/docker-resources`) provides containers and volumes management.
- `Environment Management` (`/environment-management`) lets you start an environment and run
  backend data generation with progress/status polling.
- `Performance Tests` (`/tests`) allows selecting a simulation, triggering test execution,
  listing available runs, and viewing generated HTML reports.

Additional implemented behavior:

- API errors are centrally handled through an HTTP interceptor and notification service.
- Environment options (database types, volume types, simulation types) are loaded into
  application state and reused across pages.
- The application redirects to `Info` by default on first load.

## UI libraries used

The application UI is built with:

- `Angular Material` (forms, cards, tabs, buttons, spinners, icons, sidenav, toolbar)
- `Bootstrap 5` and `Bootstrap Icons` (layout, spacing, utility classes, alerts, icons)

## OpenAPI usage

The backend contract is defined in `openapi/api-spec.yaml` and the Angular API client is generated
into `src/app/api` using OpenAPI Generator.

Generation commands:

```bash
npm run generate:api
npm run generate:api:clean
```

Notes:

- `npm install` triggers API generation automatically via `postinstall`.
- `src/app/core/services` contains app-focused wrappers around generated clients.
- `src/app/core/providers/api-config.provider.ts` wires the generated client configuration.

## Important notes

**This UI depends on the DockerManager backend, and this is crucial for correct behavior.**

The app assumes local development usage by default (localhost-based setup).

This project currently has no sophisticated environment-readiness validator. After starting an
environment, it is the user's responsibility to wait until backend services are actually ready
before starting data generation or performance tests.

For first-time use, open the `Info` page first - it summarizes feature intent and expected flow.

## Recommended usage flow

1. Open `Info` and review the process.
2. Start/prepare environment in `Environment Management`.
3. Wait until environment services are fully ready.
4. Run `Data Generation` (if required for your scenario).
5. Open `Performance Tests`, trigger a simulation, refresh runs, and inspect the report.

## Development notes

- Framework: Angular 21 (standalone component architecture).
- State approach: Angular signals/computed state (no NgRx store in current implementation).
- Notifications: `ngx-toastr`.
- Styles and theming include Angular Material theming plus Bootstrap utility classes.
