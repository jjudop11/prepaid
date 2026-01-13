# Mobile-First Prepaid Payment Platform

## Tech Stack
- **Backend**: Spring Boot 3.4+, Java 21, PostgreSQL, Redis, Kafka (KRaft), QueryDSL, Flyway.
- **Frontend**: Next.js (latest), React, Tailwind CSS v4, Zustand.

## Prerequisites
- Docker & Docker Compose
- Java 21 JDK
- Node.js 20+

## Getting Started

### 1. Infrastructure Setup
Start the required services (Postgres, Redis, Kafka):
```bash
docker compose up -d
```
Check health:
```bash
docker ps
# Ensure all containers are healthy
```

### 2. Backend Setup
Navigate to `/backend`:
```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

### 3. Frontend Setup
Navigate to `/front`:
```bash
cd front
pnpm install
pnpm dev
```

## Verification
- **Kafka**: Accessible at `localhost:29092`.
- **Kafka UI**: Accessible at `http://localhost:8085` (if enabled in docker-compose).
- **Database**: PostgreSQL 16 at `localhost:5432` (User/Pass: `postgres`/`postgres`).
- **Redis**: `localhost:6379`.
