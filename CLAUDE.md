# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WAF (Web Application Firewall) Console - A security monitoring system built with **Nginx + ModSecurity + OWASP CRS**, providing real-time attack detection, traffic analysis, and custom rule management through a SaaS-style dashboard.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Frontend :3000 │ ──▶ │  Backend :8081  │ ──▶ │   WAF :80       │
│  React + Vite   │     │  Spring Boot    │     │  ModSecurity    │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
   MySQL :3306              Redis :6379             Kafka :9092
   (Data Store)             (Cache/Session)         (Event Stream)
                                                         │
                                                         ▼
                                                  Elasticsearch :9200
                                                  (Log Search)
```

## Common Commands

### Backend (Spring Boot - Java 17)
```bash
cd backend

# Start infrastructure services
docker-compose up -d

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Run single test class
./gradlew test --tests "dev.waf.console.SomeTest"

# Build
./gradlew build

# Clean build
./gradlew clean build
```

### Frontend (React + Vite + TypeScript)
```bash
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build
npm run build

# Lint
npm run lint
```

### Docker Services
```bash
cd backend

# Start all services
docker-compose up -d

# Restart WAF only (after rule changes)
docker-compose restart waf

# View WAF logs
docker logs waf -f

# View ModSecurity detailed logs
docker logs waf 2>&1 | grep "ModSecurity"
```

## Backend Package Structure

```
dev.waf.console/
├── auth/           # Google OAuth + JWT authentication
├── user/           # User entity and repository
├── dashboard/      # Dashboard statistics and traffic data APIs
├── waflog/         # WAF log management (SUCCESS/BLOCKED/ERROR/WARNING)
├── customrule/     # Custom ModSecurity rule CRUD
├── config/         # Kafka, Redis, Elasticsearch, Cache configs
├── infrastructure/ # SecurityConfig, JWT filters, interceptors
├── service/        # Shared services (EventPublisher, AlertService)
└── event/          # Kafka event classes (AttackDetectedEvent, etc.)
```

## Key API Endpoints

| Base Path | Description |
|-----------|-------------|
| `/api/v1/auth/*` | Authentication (Google OAuth, JWT refresh) |
| `/api/dashboard/*` | Stats, status, traffic, attacks |
| `/api/logs/*` | WAF log queries and statistics |
| `/api/v1/rules/*` | Custom rule management |

## WAF Configuration

ModSecurity rules location: `waf/rules/`
- `REQUEST-900-EXCLUSION-RULES-BEFORE-CRS.conf` - Whitelist rules (False Positive fixes)
- `RESPONSE-999-EXCLUSION-RULES-AFTER-CRS.conf` - Post-CRS rules

Current settings (docker-compose.yml):
- `BLOCKING_PARANOIA: 2` - Medium-strict rule level
- `ANOMALY_INBOUND: 5` - Block threshold score

## Event-Driven Architecture

Kafka topics:
- `waf.attacks` - Attack detection events (7-day retention)
- `waf.logs` - Access logs (1-day retention)
- `waf.alerts` - Security alerts (30-day retention)

Flow: `Filebeat → Kafka → EventConsumer → Elasticsearch/AlertService`

## Tech Stack

**Backend**: Spring Boot 3.5, Spring Security, Spring Data JPA, Kafka, Redis, Elasticsearch
**Frontend**: React 18, Vite 7, TypeScript, TanStack Query, Zustand, Ant Design, ECharts, Tailwind CSS
**WAF**: OWASP ModSecurity CRS 4.x, Nginx Alpine

## Environment Setup

1. Copy `backend/.env.example` to `backend/.env`
2. Configure: `DB_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `JWT_SECRET`
3. Run `docker-compose up -d` in backend directory
4. Start backend with `./gradlew bootRun`
5. Start frontend with `npm run dev` in frontend directory
