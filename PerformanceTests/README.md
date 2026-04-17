# PerformanceTests

## Overview

`PerformanceTests` is a Gatling-based project used to run performance test sequences for database response time comparison.
The project is designed to be built and executed as a container image.

## Build the container image

Build the image from the project root:

```bash
  docker build -t docker-manager-performance-tests .
```

## Run the default simulation (debugging only)

Run the container image with the default configuration:

```bash
  docker run --rm docker-manager-performance-tests
```

The run produces a Gatling HTML report in `target/gatling`.

## Possible test sequences

| Test sequence | Simulation class | Intent                                                                   |
| --- | --- |--------------------------------------------------------------------------|
| BasicSimulation | `example.BasicSimulation` | Current main sequence for concurrent customer, item, and order requests. |
| MixedTpccSimulation | `example.MixedTpccSimulation` | Mixed TPC-C transaction mix (45/43/4/4/4) over `/api/complex/transactions`. |
| NewOrderSimulation | `example.NewOrderSimulation` | Dedicated TPC-C `/new-order` endpoint capacity test. |
| PaymentSimulation | `example.PaymentSimulation` | Dedicated TPC-C `/payment` endpoint capacity test. |
| OrderStatusSimulation | `example.OrderStatusSimulation` | Dedicated TPC-C `/order-status` endpoint capacity test. |
| DeliverySimulation | `example.DeliverySimulation` | Dedicated TPC-C `/delivery` endpoint capacity test. |
| StockLevelSimulation | `example.StockLevelSimulation` | Dedicated TPC-C `/stock-level` endpoint capacity test. |

## Environment variables

| Variable | Default value | Purpose                                                                        |
| --- | --- |--------------------------------------------------------------------------------|
| `SIMULATION_CLASS` | `example.BasicSimulation` | Selects the Gatling simulation to execute.                                     |
| `BASE_URL` | `http://host.docker.internal:8080` | Sets the base URL pointing to `MockApp` used by the HTTP protocol configuration. |
| `DATASET_SIZE` | `NOT_SET` | Dataset marker used in output folder naming. Recommended values: `small`, `medium`, `large`. |
| `DATABASE_TYPE` | `NOT_SET` | Database marker used in output folder naming. Recommended values: `MYSQL`, `POSTGRES`, `ORACLE`, `SQLSERVER`. |

The simulation reads `BASE_URL`, `DATASET_SIZE`, and `DATABASE_TYPE` through `example.utils.Config`, and the container image passes `SIMULATION_CLASS` to the Gatling run command.

After the run, the container renames the latest generated Gatling folder to `simulationName-DATABASE_TYPE-DATASET_SIZE` (for example: `MixedTpccSimulation-POSTGRES-large`).

### Shared load table (ramp duration stays at 30 seconds)

| Dataset size | Basic | Mixed TPC-C | New-Order | Payment | Order-Status | Delivery | Stock-Level |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `small` | 120 | 200 | 140 | 160 | 90 | 70 | 90 |
| `medium` | 300 | 500 | 360 | 400 | 220 | 180 | 220 |
| `large` | 600 | 1000 | 720 | 800 | 450 | 360 | 450 |

## Architecture

The project is organized around a simple execution flow:

```text
Container image
  |
  v
Simulation class selected by SIMULATION_CLASS
  |
  v
example.BasicSimulation
  |
  v
example.groups.ScenarioGroups.tpccMixedWorkload
  |
  v
Endpoint request builders in src/test/java/example/endpoints/
  |
  v
Target application base URL from BASE_URL via example.utils.Config
  |
  v
Gatling results in target/gatling
  |
  v
Latest report folder renamed to simulationName-DATABASE_TYPE-DATASET_SIZE
```

The current simulation uses these main pieces:

* `src/test/java/example/BasicSimulation.java` defines the scenario setup, injection profile, assertions, and HTTP protocol.
* `src/test/java/example/groups/ScenarioGroups.java` combines basic and TPC-C business flows.
* `src/test/java/example/endpoints/` contains reusable request builders for customer, item, order, and TPC-C transaction endpoints.
* `src/test/java/example/TpccSimulationSupport.java` defines shared injection and latency-focused assertion profiles per workload type.
* `src/test/resources/data/items.csv` provides feeder data for item identifiers.

## Notes

**This container image is used inside DockerManager, and this is crucial for correct behavior.**

A standalone container run is possible however it is for **local verification only**.
