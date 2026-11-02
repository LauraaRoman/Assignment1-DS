# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Energy Management System built as a microservices architecture for a university assignment. Three Spring Boot 4.1.1 services (Java 17), a React 19/TypeScript frontend, MySQL databases (one per service), and NGINX as an API gateway.

## Running the Project

**Full stack via Docker Compose (recommended):**
```bash
docker-compose up --build
# Frontend accessible at http://localhost (NGINX on port 80)
```

**Local development (databases via Docker, services locally):**
```bash
# Start only databases
docker-compose up user-db device-db auth-db

# Run each service (from its directory)
./mvnw spring-boot:run

# Run frontend
cd frontend && npm install && npm run dev  # http://localhost:5173
```

## Build Commands

**Java services** (run from the service directory: `auth-service/`, `user-service/`, `device-service/`):
```bash
./mvnw clean package       # Build JAR
./mvnw test                # Run tests
./mvnw test -Dtest=ClassName  # Run a single test class
```

**Frontend** (from `frontend/`):
```bash
npm run dev    # Dev server with HMR
npm run build  # Production build (TypeScript compile + Vite)
npm run lint   # Oxlint
```

## Architecture

```
Browser → NGINX:80 (API Gateway)
           ├── /auth   → auth-service:8082   (public)
           ├── /users  → user-service:8080   (JWT required)
           └── /devices → device-service:8081 (JWT validated via /validate_token)
```

Each service has its own MySQL database (`auth_db`, `user_db`, `device_db`). Services do **not** share databases.

**Inter-service communication** is synchronous REST via `RestTemplate`:
- User Service → Device Service: syncs user IDs on user create (`POST /devices/sync-user`), cascades deletes (`DELETE /devices/user/{id}`)
- Device Service URL is configurable: `${device.service.url:http://device-service:8081}`

**JWT flow:**
1. Register credentials at Auth Service → login returns a JWT (10h expiry, contains username + role)
2. Frontend stores JWT in `localStorage`, Axios interceptor injects `Authorization: Bearer` header
3. NGINX validates tokens for `/users` and `/devices` routes via `auth_request /validate_token`
4. Roles: `ADMIN` (full CRUD on users/devices) | `CLIENT` (view own devices)
5. Single admin seeded at startup: `username=admin, password=admin123` (in both auth_db and user_db)
6. ADMIN registration via `/auth/register` is blocked server-side; self-registration creates CLIENT only

**Device Service** maintains a `UserReference` table — User Service must sync a user ID there before a device can reference that user.

## Key Configuration

- Device service URL in User Service: `user-service/src/main/resources/application.properties` → `device.service.url`
- JWT secret and expiry: `auth-service/src/main/resources/application.properties`
- NGINX routing and token validation logic: `nginx.conf`
- Docker service names are used as hostnames internally (e.g., `auth-service`, `device-service`)

## OpenAPI / Swagger UI

User Service and Device Service expose Swagger UI via SpringDoc:
- User Service: `http://localhost:8080/swagger-ui.html`
- Device Service: `http://localhost:8081/swagger-ui.html`

## Package Naming

- Auth Service: `ro.utcluj.cs.authservice`
- User Service: `ro.utcluj.cs.userservice`
- Device Service: `com.utcn.deviceservice`
