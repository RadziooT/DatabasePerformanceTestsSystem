# DataGenerator

## Overview

DataGenerator is a Java Spring Boot service that creates and populates a TPC-C-style schema for a selected database
engine.

It is designed to run as a Docker container managed by DockerManager, report progress through callbacks, and exit when
work is complete.

**This app is not intended to run as a standalone module.**

## Build container image

```bash
  docker build -t docker-manager-data-generator .
```

DockerManager is responsible for passing runtime configuration and starting this image.

## Runtime behavior

- Startup runner is always active.
- On application start, generation pipeline runs automatically.
- Configuration is validated before any schema/data work starts.
- Schema/data generation is fail-fast (no retry mechanism).
- Callback delivery is best-effort (callback send errors are logged and do not stop generation flow).
- After completion/failure handling, process exits with code `0` or `1`.
- Container retention/log access should be handled by orchestrator settings (not by app keep-alive).

## Environment variables

Runtime values are read from `src/main/resources/application.yml`.

| Variable                               | Purpose                                                                             | Default in app                                                  |
|----------------------------------------|-------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `GENERATOR_JOB_ID`                     | Correlation id used in logs and callbacks.                                          | `unknown`                                                       |
| `GENERATOR_DB_TYPE`                    | Target database family (`POSTGRES`, `MYSQL`, `ORACLE`, `SQLSERVER`).                | `POSTGRES`                                                      |
| `GENERATOR_VOLUME_TYPE`                | Data volume tier (`SMALL`, `MEDIUM`, `LARGE`).                                      | `SMALL`                                                         |
| `GENERATOR_DB_NETWORK_NAME`            | Database network name/hostname used in JDBC URLs.                                  | _none_                                                          |
| `GENERATOR_DB_PORT`                    | Database port (validated at startup, no default).                                   | _none_                                                          |
| `GENERATOR_DB_NAME`                    | Database name/service (`databaseName` for SQL Server).                              | `testdb`                                                        |
| `GENERATOR_DB_USER`                    | Database user.                                                                      | `user`                                                          |
| `GENERATOR_DB_PASSWORD`                | Database password.                                                                  | `password`                                                      |
| `GENERATOR_CALLBACK_URL`               | DockerManager callback endpoint. Use routable host in containers (not `localhost`). | `http://host.docker.internal:8000/api/data-generation/callback` |
| `GENERATOR_CALLBACK_TOKEN`             | Token sent as `X-Generator-Token` header.                                           | `dev-token`                                                     |
| `GENERATOR_BATCH_SIZE`                 | Batch size used by DB insert-based loaders.                                         | `2000`                                                          |
| `GENERATOR_POOL_MIN_IDLE`              | HikariCP minimum idle connections.                                                  | `2`                                                             |
| `GENERATOR_POOL_MAX_SIZE`              | HikariCP maximum pool size.                                                         | `8`                                                             |
| `GENERATOR_POOL_CONNECTION_TIMEOUT_MS` | HikariCP connection timeout.                                                        | `30000`                                                         |
| `GENERATOR_POOL_IDLE_TIMEOUT_MS`       | HikariCP idle timeout.                                                              | `600000`                                                        |
| `GENERATOR_POOL_MAX_LIFETIME_MS`       | HikariCP max lifetime.                                                              | `1800000`                                                       |

### Validation notes

- Fail-fast validated on startup: `GENERATOR_DB_NETWORK_NAME`, `GENERATOR_DB_PORT`, `GENERATOR_DB_NAME`,
  `GENERATOR_VOLUME_TYPE`.
- `GENERATOR_DB_HOST` was removed and replaced by `GENERATOR_DB_NETWORK_NAME`.
- Required in practice for successful run: DB credentials, callback URL/token, and job id from orchestrator.

## Data volume model

Data volume is controlled by `GENERATOR_VOLUME_TYPE` and mapped to warehouse count:

- `SMALL` -> `10` warehouses
- `MEDIUM` -> `25` warehouses
- `LARGE` -> `50` warehouses

Let `W` be the selected warehouse count (`10`, `25`, or `50`).

TPC-C table row counts are generated with these formulas:

- `WAREHOUSE`: `W` rows
- `DISTRICT`: `W * 10` rows (10 districts per warehouse)
- `CUSTOMER`: `W * 10 * 3,000` rows (3,000 customers per district)
- `HISTORY`: `W * 10 * 3,000` rows
- `ORDERS`: `W * 10 * 3,000` rows (initial)
- `NEW_ORDER`: `W * 10 * 900` rows (initial, last 900 orders per district)
- `ORDER_LINE`: `W * 10 * 3,000 * 10` rows (10 lines per order)
- `STOCK`: `W * 100,000` rows (100,000 items per warehouse)
- `ITEM`: `100,000` rows (fixed, not scaled by warehouse)

### Progress percentage model

Data generation progress uses hardcoded per-table ranges tuned to reflect expected TPC-C workload proportions.

- `SCHEMA_INIT` is reported at `0%`
- `SCHEMA_CREATED` is reported at `5%`
- Data-generation callbacks (`ITEM` ... `ORDER_LINE`) move from `5%` to `100%` within fixed ranges

Current hardcoded ranges:

- `ITEM`: `5..6`
- `WAREHOUSE`: `6..7`
- `DISTRICT`: `7..8`
- `STOCK`: `8..25`
- `CUSTOMER`: `25..31`
- `HISTORY`: `31..37`
- `ORDERS`: `37..42`
- `NEW_ORDER`: `42..43`
- `ORDER_LINE`: `43..100`

Within each table range, progress is still proportional to inserted rows for that table.

## Database support

Supported database engines:

- MySQL
- PostgreSQL
- Oracle
- SQL Server

Each engine has dedicated schema/data strategy implementations under:

- `src/main/java/org/example/datagenerator/generation/strategy/schemaGeneration`
- `src/main/java/org/example/datagenerator/generation/strategy/dataGeneration`

## Generation flow

High-level pipeline (`GenerationOrchestrator`):

1. Validate configuration.
2. Send callback: `SCHEMA_INIT`.
3. Create schema using DB-specific schema strategy (single attempt, no retries).
4. Send callback: `SCHEMA_CREATED`.
5. Generate/load data table-by-table with progress callbacks.
6. Send success callback.
7. On error: send failure callback and propagate error to startup runner.
8. Startup runner attempts fallback failure callback (if needed) and exits process.

TPC-C entities populated:

- `item`
- `warehouse`
- `district`
- `stock`
- `customer`
- `history`
- `orders`
- `new_order`
- `order_line`

## Bulk loading by database

- PostgreSQL: `COPY ... FROM STDIN`
- MySQL: `LOAD DATA LOCAL INFILE`
- Oracle: batched `INSERT /*+ APPEND */` from staged TSV files
- SQL Server: batched `PreparedStatement` inserts from staged TSV files

All strategies use staged-file based bulk pipelines; generic row-by-row fallback path is not used.

## Callback contract

Callback integration is handled by `CallbackService`.

### Transport

- URL from `GENERATOR_CALLBACK_URL` (trimmed/normalized before use; only `http://` or `https://` accepted)
- Header: `X-Generator-Token: <GENERATOR_CALLBACK_TOKEN>`
- JSON payload (`Content-Type: application/json`)
- If callback host is `localhost`/`127.0.0.1` inside a container, a warning is logged.

### Payload (`ProgressCallback`)

- `jobId`
- `state` (`RUNNING`, `SUCCEEDED`, `FAILED`)
- `progressPercent` (nullable on failure)
- `currentStep`
- `message`

### Typical lifecycle

1. `SCHEMA_INIT` (`RUNNING`, 0%)
2. `SCHEMA_CREATED` (`RUNNING`, 5%)
3. Row-weighted table progress updates (`RUNNING`, monotonic from 5% toward 100%)
4. `COMPLETE` (`SUCCEEDED`, 100%)
5. On error: `FAILED`

## Project structure

```text
src/main/java/org/example/datagenerator/
  config/               # properties, datasource, startup runner wiring
  generation/           # orchestration and DB strategies
  callback/             # callback service and payload model
  model/                # enums and generation request model
```

## Important

- `GENERATOR_DB_PORT` must be explicitly set.
- Misconfiguration fails fast with clear env-var-focused messages.
- This service is intended only for DockerManager pipeline execution.

