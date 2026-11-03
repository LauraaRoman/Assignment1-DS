# Energy Management System

A microservices-based Energy Management System built with Spring Boot, React, and Docker.

## Architecture Overview

```
Browser → NGINX:80 (API Gateway / Reverse Proxy)
           ├── /auth/login     → auth-service:8082   (public)
           ├── /auth/register  → auth-service:8082   (public)
           ├── /auth/*         → auth-service:8082   (no JWT enforcement at gateway)
           ├── /users          → user-service:8080   (JWT required)
           └── /devices        → device-service:8081 (JWT required)
```

| Component       | Technology              | Port  |
|----------------|-------------------------|-------|
| Frontend        | React 19 + TypeScript   | 80    |
| auth-service    | Spring Boot 4.1.1       | 8082  |
| user-service    | Spring Boot 4.1.1       | 8080  |
| device-service  | Spring Boot 4.1.1       | 8081  |
| auth-db         | MySQL 8.0               | 3309  |
| user-db         | MySQL 8.0               | 3308  |
| device-db       | MySQL 8.0               | 3307  |

---

## Prerequisites

- **Docker Desktop** (includes Docker Compose) — required for the recommended setup
- **Java 17** — required only for local development without Docker
- **Node.js 18+** and **npm** — required only for local frontend development

---

## Running the Project

### Option 1 — Full Stack via Docker Compose (Recommended)

This is the easiest way to run the entire system. All services, databases, and the frontend are built and started together.

```bash
docker-compose up --build
```

Access the application at **http://localhost**

To stop all services:
```bash
docker-compose down
```

To stop and remove database volumes (full reset):
```bash
docker-compose down -v
```

### Option 2 — Local Development (Databases via Docker, Services Locally)

Use this when you want hot-reload on Java services or the frontend.

**Step 1 — Start only the databases:**
```bash
docker-compose up user-db device-db auth-db
```

**Step 2 — Run each Java service** (from its own directory):
```bash
# Auth Service
cd auth-service
./mvnw spring-boot:run

# User Service (new terminal)
cd user-service
./mvnw spring-boot:run

# Device Service (new terminal)
cd device-service
./mvnw spring-boot:run
```

**Step 3 — Run the frontend** (new terminal, from `frontend/`):
```bash
cd frontend
npm install
npm run dev
```

Frontend dev server runs at **http://localhost:5173**

> On Windows, use `mvnw.cmd` instead of `./mvnw`.

---

## Build Commands

### Java Services

Run these from the service directory (`auth-service/`, `user-service/`, or `device-service/`).

```bash
# Build a production JAR (required before building the Docker image manually)
./mvnw clean package

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName
```

> The Dockerfiles expect the JAR to be present at `target/*.jar`. Always run `./mvnw clean package` before building images manually.

### Frontend

Run these from the `frontend/` directory.

```bash
npm run dev      # Start development server with HMR (http://localhost:5173)
npm run build    # Type-check + production build (output in dist/)
npm run lint     # Run oxlint
npm run preview  # Preview the production build locally
```

---

## Default Credentials

A single admin account is seeded on first startup in both `auth_db` and `user_db`:

| Username | Password   | Role  |
|----------|------------|-------|
| `admin`  | `admin123` | ADMIN |

> Self-registration via `/auth/register` creates `CLIENT` accounts only. Admin registration is blocked server-side.

---

## API Endpoints

### Auth Service (public, no JWT required for login/register)

| Method | Path              | Description                  |
|--------|-------------------|------------------------------|
| POST   | `/auth/register`  | Register a new CLIENT account |
| POST   | `/auth/login`     | Login and receive a JWT token |
| GET    | `/auth/validate`  | Validate a JWT (used internally by NGINX) |

### User Service (JWT required)

| Method | Path           | Description               |
|--------|----------------|---------------------------|
| GET    | `/users`       | List all users (ADMIN)    |
| GET    | `/users/{id}`  | Get user by ID            |
| POST   | `/users`       | Create user (ADMIN)       |
| PUT    | `/users/{id}`  | Update user (ADMIN)       |
| DELETE | `/users/{id}`  | Delete user (ADMIN)       |

Swagger UI: **http://localhost:8080/swagger-ui.html**

### Device Service (JWT required)

| Method | Path              | Description                  |
|--------|-------------------|------------------------------|
| GET    | `/devices`        | List all devices (ADMIN)     |
| GET    | `/devices/{id}`   | Get device by ID             |
| POST   | `/devices`        | Create device (ADMIN)        |
| PUT    | `/devices/{id}`   | Update device (ADMIN)        |
| DELETE | `/devices/{id}`   | Delete device (ADMIN)        |

Swagger UI: **http://localhost:8081/swagger-ui.html**

---

## JWT Authentication Flow

1. POST to `/auth/login` with credentials → receive a JWT (10-hour expiry).
2. Include the token in subsequent requests: `Authorization: Bearer <token>`.
3. NGINX validates the token for `/users` and `/devices` routes via an internal subrequest to `/auth/validate`.
4. The JWT contains the username and role (`ADMIN` or `CLIENT`).

---

## Configuration

### Environment Variables

All services read configuration from environment variables, with sensible local-development defaults.

**auth-service**

| Variable     | Default                             | Description         |
|--------------|-------------------------------------|---------------------|
| `SERVER_PORT`| `8082`                              | Service port        |
| `DB_IP`      | `localhost`                         | Database host       |
| `DB_PORT`    | `3306`                              | Database port       |
| `DB_DBNAME`  | `auth_db`                           | Database name       |
| `DB_USER`    | `root`                              | Database user       |
| `DB_PASSWORD`| `root`                              | Database password   |
| `JWT_SECRET` | `3f8a2b1c4d5e...` (default insecure)| JWT signing secret  |

**user-service**

| Variable            | Default                        | Description               |
|---------------------|--------------------------------|---------------------------|
| `SERVER_PORT`       | `8080`                         | Service port              |
| `DB_IP`             | `localhost`                    | Database host             |
| `DB_PORT`           | `3306`                         | Database port             |
| `DB_DBNAME`         | `user_db`                      | Database name             |
| `DB_USER`           | `root`                         | Database user             |
| `DB_PASSWORD`       | *(empty)*                      | Database password         |
| `DEVICE_SERVICE_URL`| `http://device-service:8081`   | Device service base URL   |
| `AUTH_SERVICE_URL`  | `http://auth-service:8082`     | Auth service base URL     |

**device-service**

| Variable     | Default     | Description       |
|--------------|-------------|-------------------|
| `SERVER_PORT`| `8081`      | Service port      |
| `DB_IP`      | `localhost` | Database host     |
| `DB_PORT`    | `3306`      | Database port     |
| `DB_DBNAME`  | `device_db` | Database name     |
| `DB_USER`    | `root`      | Database user     |
| `DB_PASSWORD`| *(empty)*   | Database password |

---

## Inter-Service Communication

- **User Service → Device Service**: when a user is created, the user ID is synced to the Device Service (`POST /devices/sync-user`). On user deletion, devices are cascade-deleted (`DELETE /devices/user/{id}`).
- The Device Service maintains a `UserReference` table. A device cannot reference a user that has not been synced.

---

## Database Ports (Host Machine)

When running with Docker Compose, the databases are exposed on the host for direct access (e.g., via a DB client):

| Database   | Host Port |
|------------|-----------|
| `auth_db`  | 3309      |
| `device_db`| 3307      |
| `user_db`  | 3308      |

Connection string example: `jdbc:mysql://localhost:3308/user_db` (user: `root`, password: `root`)

---

## Project Structure

```
Assignment-1/
├── auth-service/        # Spring Boot — authentication & JWT issuance
├── user-service/        # Spring Boot — user management
├── device-service/      # Spring Boot — device management
├── frontend/            # React 19 + TypeScript + Vite
├── DiagramaDeDeplyment/ # Deployment diagram (draw.io)
├── docker-compose.yml   # Orchestrates all services and databases
└── nginx.conf           # API gateway routing and JWT validation
```
