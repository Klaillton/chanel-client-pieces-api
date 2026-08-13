# Chanel Client Pieces API

A Spring Boot project that loads Chanel pieces from a CSV file into an in-memory repository at application startup.

This repository currently focuses on domain modeling and data ingestion, with no public REST endpoints implemented yet.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [CSV Loading Rules](#csv-loading-rules)
- [Design Decisions](#design-decisions)
- [Known Limitations](#known-limitations)
- [Contributing](#contributing)

## Overview

The application reads `pieces.csv` from the classpath when it starts. Each valid record is converted into a `Piece` object and saved in an in-memory repository.

Invalid records are skipped and logged as warnings to keep the ingestion process resilient.

## Features

- CSV ingestion on startup using `ApplicationRunner`
- Input parsing with Apache Commons CSV
- Domain modeling with `Piece` and `PieceType`
- In-memory storage via a thread-safe repository implementation
- Graceful handling of invalid rows (skip + warning log)

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Apache Commons CSV 1.10.0
- Lombok
- Maven

## Project Structure

```text
src/main/java/com/channel/pieces
|- PiecesApplication.java
|- domains/
|  |- Piece.java
|  |- PieceType.java
|- dto/
|  |- PieceResponse.java
|- infrastructure/
|  |- CsvPieceLoader.java
|- repository/
	|- PieceRepository.java
	|- InMemoryPieceRepository.java
```

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+ (or use the Maven Wrapper included in this repo)

### Run the Application

On Windows:

```bash
./mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

### Run Tests

On Windows:

```bash
./mvnw.cmd test
```

On Linux/macOS:

```bash
./mvnw test
```

## CSV Loading Rules

The loader expects a semicolon-separated file (`;`) with the following columns:

1. `serial_number`
2. `reference`
3. `size`
4. `type`
5. `production_date` (ISO-8601 date, for example `2023-06-27`)

Validation behavior:

- Missing required fields raise a validation error for that row.
- Unknown/blank `type` values raise a validation error for that row.
- Invalid dates raise a validation error for that row.
- Invalid rows are skipped and counted as `skipped` in logs.
- Valid rows are persisted in memory and counted as `loaded` in logs.

## Design Decisions

### Data Storage

Data is stored in memory using `CopyOnWriteArrayList`.

Reasoning:

- Small dataset expected
- Zero external dependencies for local execution
- Fast implementation focused on domain and ingestion behavior

### Domain Model

- `Piece` as a domain entity
- `PieceType` as an enum for type safety
- `productionDate` represented as `LocalDate`

### Invalid Data Handling

Instead of failing the full import, the loader skips invalid rows and logs warnings.

This trade-off prioritizes availability and partial ingestion over strict all-or-nothing processing.

### Startup Loading Strategy

CSV data is loaded at startup through Spring Boot `ApplicationRunner`, ensuring repository data is available as soon as the app is ready.

## Known Limitations

- No REST controller is implemented yet.
- Data is not persisted across restarts.
- There is no deduplication strategy for potential business-key duplicates (`serial_number + reference + size`).

## Contributing

Contributions are welcome.

Suggested flow:

1. Fork the repository
2. Create a feature branch
3. Commit your changes with clear messages
4. Open a pull request with context and test evidence
