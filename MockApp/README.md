# MockApp

## Overview

MockApp is a Java Spring Boot CRUD application implementing the TPC-C schema.

It exposes endpoints consumed by the `PerformanceTests` application.

This app is intended to run as a container managed by `DockerManager`.

## Build the container image

From the project root, run:

```bash
  docker build -t docker-manager-mock-app .
```

## Environment variables

MockApp reads database connection settings from profile-specific Spring configuration files.

| Variable | Purpose | Typical usage |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the active database profile. DockerManager is responsible for passing the correct profile into the container. | `h2`, `mysql`, `postgres`, `oracle`, `sqlserver` |
| `DB_HOST` | Database host or container name. | Used by `mysql`, `postgres`, `oracle`, `sqlserver` profiles |
| `DB_PORT` | Database port. | Used by `mysql`, `postgres`, `oracle`, `sqlserver` profiles |
| `DB_NAME` | Database name or service name. | Used by `mysql`, `postgres`, `oracle`, `sqlserver` profiles |
| `DB_USERNAME` | Database username. | Used by `mysql`, `postgres`, `oracle`, `sqlserver` profiles |
| `DB_PASSWORD` | Database password. | Used by `mysql`, `postgres`, `oracle`, `sqlserver` profiles |

Profile defaults defined in `src/main/resources`:

- `application-mysql.yml`: `DB_HOST=mysql`, `DB_PORT=3306`, `DB_NAME=mockapp`, `DB_USERNAME=mockapp`, `DB_PASSWORD=mockapp`
- `application-postgres.yml`: `DB_HOST=postgres`, `DB_PORT=5432`, `DB_NAME=mockapp`, `DB_USERNAME=mockapp`, `DB_PASSWORD=mockapp`
- `application-oracle.yml`: `DB_HOST=oracle-free`, `DB_PORT=1521`, `DB_NAME=FREEPDB1`, `DB_USERNAME=mockapp`, `DB_PASSWORD=mockapp`
- `application-sqlserver.yml`: `DB_HOST=sqlserver`, `DB_PORT=1433`, `DB_NAME=mockapp`, `DB_USERNAME=mockapp`, `DB_PASSWORD=mockapp`
- `application-h2.yml`: uses an in-memory database and initializes schema and data on every run

## Supported databases

Supported databases:

- `h2`
- `mysql`
- `postgres`
- `oracle`
- `sqlserver`

## Profile behavior

- `src/main/resources/application.yml` defaults to the `dev` profile.
- DockerManager should override the profile when running the container so the correct database configuration is used.
- When `h2` is selected, the app uses an in-memory database which reloads schema and seed data on each startup.

## Architecture

MockApp follows a layered structure with clear separation across API, shared utilities, business logic, and persistence.

```text
DockerManager / container run
  |
  v
SPRING_PROFILES_ACTIVE + DB_* environment variables
  |
  v
src/main/java/com/example/mockapp/
  |
  +--> api/
  |      +--> *Controller
  |      +--> mapper/
  |      +--> model/
  |              +--> request/response objects
  |
  +--> common/
  |      +--> config/
  |      +--> exception/
  |
  +--> domain/
  |      +--> *Service
  |      +--> mapper/
  |      +--> model/
  |              +--> business domain objects
  |
  +--> persistence/
         +--> *Repository
         +--> model/
                 +--> *Entity
  |
  v
Database
```

Key symbols and responsibilities:

- `api/*Controller` exposes REST endpoints.
- `api/*/model` contains request and response objects.
- `domain/*Service` contains business logic.
- `domain/*/model` contains domain objects.
- `persistence/*Repository` contains Spring Data repositories.
- `persistence/*/model` contains JPA entities.
- `common/config` contains shared configuration (for example, OpenAPI setup).
- `common/exception` contains shared exception handling types.

## Additional info

- OpenAPI documentation is available through the Springdoc UI.
- For normal usage, run this app inside DockerManager.

## Local verification only

A standalone container run is available for local verification only.

```bash
  docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=h2 docker-manager-mock-app
```

When run this way, `h2` generates fresh data on each run because the database is in-memory and schema/data scripts are re-applied at startup.

## TPC-C Schema Compliance

MockApp is built on strict TPC-C schema compliance with the following implementation details:

- **Composite Primary Keys**: CUSTOMER, DISTRICT, ORDERS, NEW_ORDER, ORDER_LINE, and STOCK tables use composite primary keys as defined in the TPC-C specification.
- **HISTORY Technical Surrogate**: The HISTORY table includes a technical surrogate `H_ID` column for JPA identity management, but this field is **not part of TPC-C business semantics** and must not be considered in any performance benchmarking or transaction validation. All TPC-C operations should reference HISTORY by business keys only: (H_C_W_ID, H_C_D_ID, H_C_ID), (H_W_ID, H_D_ID).
- **Foreign Key Constraints**: All relationships strictly enforce TPC-C-defined foreign keys at the database level.
- **Indexes**: Critical performance indexes are defined for New-Order, Payment, Order-Status, Delivery, and Stock-Level transaction paths.
- **Integer Year-To-Date**: The STOCK table uses INTEGER for S_YTD (not NUMERIC) for strict TPC-C alignment.

## TPC-C transaction examples (H2 demo data)

Base URL:

`http://localhost:8080/api/complex/transactions`

All endpoints below are `POST` and match the seeded demo data in `h2` profile.

### 1) New-Order

Endpoint: `/new-order`

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "customerId": 2,
  "allLocal": false,
  "items": [
    { "itemId": 3, "quantity": 2 },
    { "itemId": 5, "supplyWarehouseId": 2, "quantity": 1 }
  ]
}
```

### 2) Payment

Endpoint: `/payment`

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "customerId": 1,
  "amount": 25.00,
  "data": "counter payment"
}
```

### 3) Order-Status

Endpoint: `/order-status`

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "customerId": 2
}
```

### 4) Delivery

Endpoint: `/delivery`

```json
{
  "warehouseId": 1,
  "carrierId": 7,
  "districtIds": [1, 2]
}
```

### 5) Stock-Level

Endpoint: `/stock-level`

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "threshold": 10,
  "recentOrderCount": 20
}
```

