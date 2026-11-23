# Energy Management System — Assignment 2

Distributed Systems, 2026-2027  
Asynchronous Communication with RabbitMQ

---

## Architecture Overview

```
Browser → NGINX :8888 (API Gateway / Reverse Proxy)
           ├── /auth       → auth-service:8082     (public)
           ├── /users      → user-service:8080     (JWT required)
           ├── /devices    → device-service:8081   (JWT required)
           └── /monitoring → monitoring-service:8083 (JWT required)

Simulator (standalone JAR — not in Docker)
    └── publishes <timestamp, device_id, measurement_value>
           └── rabbitmq-data:5673  (device.data.exchange — FanoutExchange)
                   └── monitoring-service consumes → HourlyConsumption DB

user-service / device-service
    └── publish sync events on user/device create
           └── rabbitmq-sync:5672  (sync.exchange — FanoutExchange)
                   ├── device-service consumes → UserReference DB
                   └── monitoring-service consumes → DeviceReference DB
```

### Services

| Container | Port | Description |
|---|---|---|
| reverse-proxy | 8888 | NGINX API Gateway |
| auth-service | 8082 | JWT authentication |
| user-service | 8080 | User management (ADMIN/CLIENT) |
| device-service | 8081 | Device management + Swagger UI |
| monitoring-service | 8083 | Hourly energy consumption aggregation |
| rabbitmq-sync | 5672 / 15672 | Sync broker (user/device events) |
| rabbitmq-data | 5673 / 15673 | Data broker (simulator measurements) |
| user-db | 3308 | MySQL — user_db |
| device-db | 3307 | MySQL — device_db |
| auth-db | 3309 | MySQL — auth_db |
| monitoring-db | 3310 | MySQL — monitoring_db |

**Simulator** runs as a standalone JAR (not deployed in Docker).

---

## Prerequisites

- **Docker Desktop** — required for the full stack
- **Java 17+** — required for running the simulator locally

---

## Build & Run

### Full stack via Docker Compose

```bash
docker-compose up --build
```

Frontend available at: `http://localhost:8888`

RabbitMQ management consoles:
- Sync broker: `http://localhost:15672` (guest / guest)
- Data broker: `http://localhost:15673` (guest / guest)

```bash
docker-compose down        # stop
docker-compose down -v     # stop + remove DB volumes (full reset)
```

---

## Running the Simulator

The simulator is a standalone Spring Boot JAR — it is **not** part of the Docker Compose stack.

### Build the JAR (no local Maven needed)

```bash
# From the simulator/ directory:
docker run --rm -v "%CD%:/app" -w //app maven:3.9-eclipse-temurin-17-alpine mvn package -DskipTests -q
```

JAR output: `simulator/target/simulator-0.0.1-SNAPSHOT.jar`

### Run modes

**Backfill a full day for a specific device (exits when done):**
```bash
java -jar simulator/target/simulator-0.0.1-SNAPSHOT.jar ^
  --simulator.device-ids=<device-uuid> ^
  --date=2026-11-23
```

**Backfill for multiple devices:**
```bash
java -jar simulator/target/simulator-0.0.1-SNAPSHOT.jar ^
  --simulator.device-ids=uuid1,uuid2,uuid3 ^
  --date=2026-11-23
```

**Backfill all devices (fetched automatically from device-service):**
```bash
java -jar simulator/target/simulator-0.0.1-SNAPSHOT.jar --date=2026-11-23
```

**Run continuously — sends one measurement per device every 10 minutes:**
```bash
java -jar simulator/target/simulator-0.0.1-SNAPSHOT.jar --simulator.device-ids=<device-uuid>
```

> The simulator connects to `rabbitmq-data` on `localhost:5673`. The Docker stack must be running first.

### Finding device IDs

Open Swagger UI at `http://localhost:8081/swagger-ui.html` → `GET /devices` → Execute.

---

## Demo Flow

1. `docker-compose up --build`
2. Open `http://localhost:8888`, log in as `admin` / `admin123`
3. Create a user and a device via the UI
4. Copy the device ID from Swagger (`http://localhost:8081/swagger-ui.html`)
5. Run the simulator:
   ```bash
   java -jar simulator/target/simulator-0.0.1-SNAPSHOT.jar --simulator.device-ids=<uuid> --date=2026-11-23
   ```
6. Log in as the CLIENT user who owns that device
7. Navigate to **Energy** tab → select the date → view the hourly bar chart

---

## Message Broker Design

Two separate RabbitMQ instances, both using **Publish-Subscribe (FanoutExchange)**:

**rabbitmq-sync** (port 5672) — synchronization events:
- Exchange: `sync.exchange` (FanoutExchange)
- Publishers: user-service (USER created), device-service (DEVICE created/updated)
- Consumers:
  - `sync.queue.device-service` → device-service stores `UserReference`
  - `sync.queue.monitoring-service` → monitoring-service stores `DeviceReference`

**rabbitmq-data** (port 5673) — sensor measurements:
- Exchange: `device.data.exchange` (FanoutExchange)
- Publisher: simulator `<timestamp, device_id, measurement_value>`
- Consumer: `device.data.queue` → monitoring-service aggregates into hourly totals

---

## Default Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |

> Self-registration via `/auth/register` creates CLIENT accounts only. ADMIN registration is blocked server-side.

---

## API Endpoints

### Auth Service (public)

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a CLIENT account |
| POST | `/auth/login` | Login, receive JWT |
| GET | `/auth/validate` | Validate JWT (used internally by NGINX) |

### User Service (JWT required)

| Method | Path | Description |
|---|---|---|
| GET | `/users` | List all users (ADMIN) |
| GET | `/users/{id}` | Get user by ID |
| GET | `/users/by-username/{username}` | Get user by username |
| POST | `/users` | Create user (ADMIN) |
| PUT | `/users/{id}` | Update user (ADMIN) |
| DELETE | `/users/{id}` | Delete user (ADMIN) |

Swagger: `http://localhost:8080/swagger-ui.html`

### Device Service (JWT required)

| Method | Path | Description |
|---|---|---|
| GET | `/devices` | List all devices (ADMIN) |
| GET | `/devices/{id}` | Get device by ID |
| GET | `/devices/user/{userId}` | Get devices for a user |
| POST | `/devices` | Create device (ADMIN) |
| PUT | `/devices/{id}` | Update device (ADMIN) |
| DELETE | `/devices/{id}` | Delete device (ADMIN) |

Swagger: `http://localhost:8081/swagger-ui.html`

### Monitoring Service (JWT required)

| Method | Path | Description |
|---|---|---|
| GET | `/monitoring/user/{userId}?date=YYYY-MM-DD` | Hourly consumption for all user devices |
| GET | `/monitoring/device/{deviceId}?date=YYYY-MM-DD` | Hourly consumption for one device |

---

## JWT Authentication Flow

1. POST `/auth/login` → receive JWT (10h expiry, contains username + role)
2. Frontend stores JWT in `localStorage`, Axios injects `Authorization: Bearer` header
3. NGINX validates token for `/users`, `/devices`, `/monitoring` via internal subrequest to `/auth/validate`
4. Roles: `ADMIN` (full CRUD) | `CLIENT` (view own devices and energy chart)

---

## Project Structure

```
├── auth-service/         Spring Boot — JWT authentication
├── user-service/         Spring Boot — user management
├── device-service/       Spring Boot — device management
├── monitoring-service/   Spring Boot — energy aggregation consumer
├── simulator/            Spring Boot standalone JAR — data producer
├── frontend/             React 19 + TypeScript + MUI
├── A2/                   Deployment diagram + assignment PDF
├── docker-compose.yml
├── nginx.conf
└── README.md
```

---

## Database Ports (host machine)

| Database | Host Port |
|---|---|
| auth_db | 3309 |
| user_db | 3308 |
| device_db | 3307 |
| monitoring_db | 3310 |
