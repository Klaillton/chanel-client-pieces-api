# Chanel Client Pieces API

> **Original submission:** the take-home was delivered on 2026-08-13 under the requested 1h30 timebox, written without AI. That exact version is preserved on the [`submission-2026-08-13` branch](https://github.com/Klaillton/chanel-client-pieces-api/tree/submission-2026-08-13) and [tag](https://github.com/Klaillton/chanel-client-pieces-api/tree/refs/tags/submission-2026-08-13). `main` keeps evolving from there.

Spring Boot backend that integrates piece data from an external CSV and exposes it through a REST API for the mobile app and back-office.

## Overview

The application loads `pieces.csv` from the classpath at startup into an in-memory store and exposes read-only endpoints to list and retrieve pieces. Invalid rows are skipped so partial data remains available.

This solution prioritizes clarity, quick decisions under a 3-hour timebox, and explicit documentation of assumptions.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Apache Commons CSV 1.10.0
- Lombok
- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Getting Started

### Prerequisites

- JDK 21

### Run the application

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
./mvnw.cmd spring-boot:run
```

Application base URL: `http://localhost:8080`

### Run tests

Linux / macOS:

```bash
./mvnw test
```

Windows:

```bash
./mvnw.cmd test
```

## API

Base path: `/api/pieces`

Response body (`PieceResponse`) for both list and detail:

| Field           | Type   | Description                                      |
|-----------------|--------|--------------------------------------------------|
| id              | number | Technical ID generated at load time              |
| type            | string | `WATCH` or `JEWELRY`                             |
| serialNumber    | string | Serial number from the CSV                       |
| size            | string | Size from the CSV                                |
| productionDate  | string | ISO-8601 date (`yyyy-MM-dd`), or `null`          |
| reference       | string | Reference from the CSV                           |

### GET /api/pieces

Lists pieces. All filters are optional and combinable.

| Parameter  | Type   | Required | Description                          |
|------------|--------|----------|--------------------------------------|
| type       | enum   | no       | `WATCH` or `JEWELRY`                 |
| reference  | string | no       | Exact match (case-insensitive)       |
| size       | string | no       | Exact match (case-insensitive)       |

An unknown `type` value (for example `type=FOO`) is rejected by Spring as a type mismatch and returns **400**.

Examples:

```bash
curl "http://localhost:8080/api/pieces"
curl "http://localhost:8080/api/pieces?type=WATCH"
curl "http://localhost:8080/api/pieces?type=WATCH&reference=J12&size=38MM"
```

### GET /api/pieces/{id}

Returns a single piece by its technical ID.

| Status | Meaning                                                                 |
|--------|-------------------------------------------------------------------------|
| 200    | Piece found                                                             |
| 400    | Invalid ID (null, ≤ 0, or non-numeric) **or** piece does not exist      |
| 404    | Piece does not exist                                                  |

`PieceService.findById` throws `IllegalArgumentException` both for an invalid ID and for a missing piece. The global handler maps that exception to **400**.

The handler also maps `NoSuchElementException` to **404**, but the service does not throw that exception today. Controller tests describe the 404 intent by stubbing `NoSuchElementException`; the live contract is 400.

Example:

```bash
curl "http://localhost:8080/api/pieces/1"
```

Error response shape (`ErrorResponse`):

```json
{
  "timestamp": "2026-08-13T17:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Piece not found with id: 999",
  "path": "/api/pieces/999"
}
```

## Architecture

```
Controller  →  Service  →  Repository (in-memory)
                              ↑
                     CsvPieceLoader (ApplicationRunner)
```

- **Controller**: HTTP only (status codes, request params, path variables).
- **Service**: input checks, lookup, and mapping to DTOs.
- **Repository**: storage abstraction (`CopyOnWriteArrayList` + `AtomicLong` sequence).
- **CsvPieceLoader**: reads the CSV at startup; does not leak into the controller.

## Design Decisions

### Data storage

In-memory (`CopyOnWriteArrayList` + sequential technical `Long` ID starting at 1).

**Why**: small dataset, zero configuration, fast to implement and test within 3 hours. Switching to a real database later would only require a new repository implementation.

### Identifier

- Business key: `serialNumber + reference + size` (as stated in the brief).
- API identifier: technical `Long id` generated on load.

**Why**: simpler URL design for mobile/back-office consumers. Filters cover the business key combinations without forcing a composite path.

### Filtering

Query parameters that can be combined (`type`, `reference`, `size`). Blank `reference` / `size` values are treated as "no filter".

**Why**: covers the story screens without over-engineering URL design.

### Type modeling

`PieceType` enum (`WATCH`, `JEWELRY`) with CSV parsing that normalizes `jewellery` → `JEWELRY` (case-insensitive).

**Why**: type-safety and cleaner domain code (Clean Code / SRP).

### Invalid data handling

Rows with invalid date (for example `2021-11-31` in the sample file), unknown type, or missing required fields are **skipped** and logged as warnings. The application continues with the valid rows.

If the CSV file itself cannot be read, the loader logs a warning and starts with whatever was already loaded (usually nothing). If the repository already has data, startup loading is skipped.

**Why**: partial data is preferable to a failed full load for a client-facing catalogue.

### CSV loading

- Source file: `src/main/resources/pieces.csv` (classpath).
- Loaded once at startup via `ApplicationRunner`.
- Library: Apache Commons CSV.
- Delimiter: `;` (as in the provided sample file).
- Header row is skipped. Empty lines are ignored. Fields are trimmed.

Expected columns (positional, no named header mapping):

1. `serial_number`
2. `reference`
3. `size`
4. `type`
5. `production_date` (ISO-8601 date, for example `2023-06-27`)

## Assumptions

1. The provided CSV is the source of truth and fits in memory.
2. Partial ingestion is acceptable when some rows are invalid.
3. Consumers need a simple technical ID plus filterable listing more than a composite-key path.
4. Exact match (case-insensitive) is sufficient for `reference` and `size`.
5. No authentication/authorization is required for this first iteration.
6. Read-only API is enough for the current story.

## Questions for the Product Team

1. Should a single invalid row abort the entire CSV import, or is skipping + logging preferred?
2. Do mobile / back-office need an endpoint by the composite business key (`serialNumber + reference + size`), or is filtered listing + technical ID enough?
3. How should duplicate business keys be handled (keep first, keep last, reject)?
4. Is pagination required for the list endpoint in the near term?
5. Should `productionDate` be exposed in the list response or only in the detail endpoint? (today it is present on both, via the same DTO)

## What I implemented

- Spring Boot project with clear package structure
- Domain model (`Piece`, `PieceType`)
- CSV loading at startup with invalid-row handling
- In-memory repository with technical ID generation
- `GET /api/pieces` with combinable filters
- `GET /api/pieces/{id}`
- Global exception handling (`IllegalArgumentException` / type mismatch → 400, `NoSuchElementException` → 404, other `Exception` → 500)
- Unit tests for Service and MockMvc tests for Controller
- README documenting decisions, assumptions and prioritization

## What is left / What I would improve

- Align missing-piece lookup with HTTP 404 (`NoSuchElementException` in the service, as the tests already expect)
- Deduplication on the business key (`serialNumber + reference + size`)
- Pagination and sorting on the list endpoint
- Stronger validation layer (`spring-boot-starter-validation` is already on the classpath; Bean Validation annotations are not used yet)
- Persistence (JPA + real database) if the dataset grows
- OpenAPI / Swagger documentation
- Higher test coverage and integration tests against the real CSV
- Endpoint by composite business key if product confirms the need

## Time Prioritization (3-hour timebox)

| Priority     | Item                                              | Status   |
|--------------|---------------------------------------------------|----------|
| Must Have    | Spring Boot + CSV load + domain model             | Done     |
| Must Have    | In-memory store + GET list with filters           | Done     |
| Must Have    | Error handling + README with decisions            | Done     |
| Should Have  | GET by ID                                         | Done     |
| Should Have  | Essential unit / MockMvc tests                    | Done     |
| Nice to Have | Deduplication, pagination, Swagger, real DB       | Not done |

## Testing

- `PieceServiceTest`: list without filters, list with `type` filter, found by ID, not found, invalid id (`null` / `0` / `-1`)
- `PiecesControllerTest` (MockMvc): list 200, filtered list 200, get-by-id 200, stubbed 404, stubbed 400
- `PiecesApplicationTests`: Spring context load

Note: `PieceServiceTest` expects `NoSuchElementException` when the piece is missing, but `PieceService` currently throws `IllegalArgumentException`. The controller 404 test stubs that exception on the service and does not exercise the live lookup path.

```bash
./mvnw test
```

## Project Structure

```text
src/main/java/com/channel/pieces
├── PiecesApplication.java
├── controller/
│   └── PiecesController.java
├── domains/
│   ├── Piece.java
│   └── PieceType.java
├── dto/
│   ├── PieceResponse.java
│   └── ErrorResponse.java
├── exceptions/
│   └── GlobalExceptionHandler.java
├── infrastructure/
│   └── CsvPieceLoader.java
├── repository/
│   ├── PieceRepository.java
│   └── InMemoryPieceRepository.java
└── service/
    └── PieceService.java

src/main/resources
└── pieces.csv

src/test/java/com/channel/pieces
├── PiecesApplicationTests.java
├── controller/
│   └── PiecesControllerTest.java
└── service/
    └── PieceServiceTest.java
```
