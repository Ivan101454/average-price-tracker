# Average Price Tracker

A Spring Boot service designed to automatically fetch, store, and analyze the average trading price of crypto pairs.

## Tech Stack
* **Java 21** / Spring Boot 3.3.5
* **Spring Cloud OpenFeign** for declarative, type-safe REST communication with Binance
* **PostgreSQL 16** (Advanced schema featuring Time-Based Partitioning)
* **Flyway** for reliable database schema migrations
* **Testcontainers & WireMock** for robust integration testing in simulated production environments
* **Docker & Docker Compose** for streamlined containerization and one-click deployment

## Key Architectural Decisions & Code Review Highlights

1. **IP Ban Protection (HTTP 418/429 Back-off):** Integrated a custom error decoder (`BinanceErrorDecoder`) into OpenFeign. When Binance issues a `429 Too Many Requests` status, the decoder extracts the exact wait time from the `Retry-After` header and updates a thread-safe `BinanceRateLimitManager`. The service preemptively skips scheduled polls during active bans, preventing the system from ignoring rate-limit rules and getting hard-banned (HTTP 418).
2. **Trend Calculation Optimization:** The market trend endpoint (`UPWARD`, `DOWNWARD`, `FLAT`) is highly optimized at the database level. Instead of pulling the entire price history into Java memory (`List`), which introduces severe `OutOfMemoryError` risks during large date range queries, the repository issues two lightweight, targeted queries fetching only the earliest and latest price entries within the specified timeframe.
3. **Time-Based Partitioning:** The `price` table utilizes PostgreSQL's native range partitioning (`PARTITION BY RANGE (fetched_at)`). Breaking historical time-series data into chunks ensures sub-second analytical query execution even when handling millions of rows. For production stability, a `DEFAULT` partition is included to capture edge-case timestamps safely.

## Environment Variables
* `BINANCE_POLL_INTERVAL` — The background fetch frequency specified as an ISO-8601 duration string (default: `PT30S` — 30 seconds).
* `APP_DEFAULT_SYMBOL` — The target trading pair used if no query parameter is provided (default: `BNBUSDT`).
* `SPRING_DATASOURCE_URL` — Dynamic JDBC connection string targeting the database container.

## Quick Start

Execute the following single command in the repository root:

```bash
docker-compose up --build
```
The API web server will map to: `http://localhost:8080`

## 📯 REST API Endpoint Showcase

### 1. Retrieve the Latest Tracked Price
```bash
GET http://localhost:8080/api/v1/prices/latest?symbol=BNBUSDT
```

### 2. Calculate the Period Average
```bash
GET http://localhost:8080/api/v1/prices/average?symbol=BNBUSDT&from=2026-10-28T00:00:00Z&to=2026-10-28T23:59:59Z
```

### 3. Fetch Price History (With Pagination & Sorting)
```bash
GET http://localhost:8080/api/v1/prices?symbol=BNBUSDT&from=2026-10-28T00:00:00Z&to=2026-10-28T23:59:59Z&page=0&size=20
```

### 4. Evaluate Price Trend
```bash
GET http://localhost:8080/api/v1/prices/trend?symbol=BNBUSDT&from=2026-10-28T00:00:00Z&to=2026-10-28T23:59:59Z
```

## 🧪 Running the Test Suite

To run tests locally, invoke Maven:
```bash
mvn clean test
```
