# Production Hardening & Operations Guide

This document captures the enterprise hardening applied to the AI Disaster
Management System and the exact configuration required for a production
(PostgreSQL) deployment.

---

## Security Posture

### Authentication
- Passwords are hashed with **BCrypt**.
- **Account lockout**: after `LOGIN_MAX_ATTEMPTS` (default 5) failed logins for
  the same username, the account is locked for `LOGIN_LOCKOUT_MINUTES` (default
  15) minutes. Lockout keys are normalized (case-insensitive, trimmed).
- **Rate limiting** (in-memory sliding window, per IP):
  - `/api/auth/**`: `RATE_LIMIT_MAX_PER_MINUTE` (default 60).
  - `/api/public/**`: `RATE_LIMIT_PUBLIC_MAX_PER_MINUTE` (default 20).
  - The bucket key is always the **real remote address** plus (as a hint) the
    first `X-Forwarded-For` entry, so spoofed forwarded headers cannot bypass
    limits. Stale buckets are evicted periodically.
  - Applies only to auth and public endpoints so internal API traffic is
    unaffected.
- **JWT**: signed with HMAC-SHA256 using a secret that must decode to **at least
  32 bytes (256 bits)**. The server fails fast at startup if `JWT_SECRET` is
  missing or too weak. Tokens carry an `issuer` claim
  (`ai-disaster-management`). `/api/auth/me` is null-safe.

### Password policy
New and reset passwords must be at least 8 characters and contain an uppercase
letter, a lowercase letter, and a digit (enforced in `UserServiceImpl`).

### Transport & response headers
Enforced via Spring Security in `SecurityConfig`:
- `Strict-Transport-Security` (includeSubDomains, 1 year).
- `Content-Security-Policy`: `default-src 'self'`, script/style `'unsafe-inline'`
  (needed for the MUI app), images allow `data:` + `https:`, connect allows
  `ws:`/`wss:`, and `frame-ancestors 'self'`.
- `Referrer-Policy: same-origin`.
- `Permissions-Policy`: geolocation self; camera and microphone blocked.
- `X-Frame-Options: SAMEORIGIN`; `X-Content-Type-Options: nosniff`.

### PII protection
- `DisasterResponse` carries a `redactReporterInfo()` method that strips the
  reporter's mobile number and email.
- **WebSocket disaster broadcasts always use the redacted DTO**, so citizen
  reporter contact details are never pushed to connected clients.
- CORS is restricted to `CORS_ORIGINS` (comma-separated list).

### Input validation & file uploads
- `@Valid` is enforced on all create/update request bodies.
- Disaster coordinates bounded (`@DecimalMin`/`@DecimalMax`) and attachments
  limited to **5 per report**, with a `dataUrl` cap of ~7 MB and a binary cap of
  5 MB (`MAX_FILE_SIZE`/`MAX_REQUEST_SIZE` multipart limits).
- Attachment MIME type must be in the allow-list (`image/jpeg`, `image/png`,
  `image/webp`, `video/mp4`, `application/pdf`); invalid categories are
  rejected with a friendly error instead of silently defaulting.

### Operational lockdown
- **H2 console is disabled** in the production profile.
- Actuator exposure limited to `health,info,metrics`; health details are only
  shown to authorized users.
- `spring.jpa.hibernate.ddl-auto=validate` (schema drift is caught at boot).
- The dev-only `DataSeeder` is annotated `@Profile("!postgres")`, so demo data
  never loads in production.

---

## Database & Flyway

Migrations under `backend/src/main/resources/db/migration/`:

| Version | Purpose |
|---------|---------|
| V1 | Base schema (users, disasters, hospitals, shelters, volunteers, resources, drones, notifications, audit_logs, case_studies, rescue_teams, team_members, tokens) |
| V2 | Public report fields |
| V3 | Disaster enrichment (comments, assignments) |
| V4 | Disaster attachments |
| V5 | Rescue team coordination (missions, events, shifts, vehicles, equipment) |
| V6 | Mission assets and resource movements |
| V7 | **Performance indexes**: geo `(latitude, longitude)` on hospitals, shelters, volunteers, resources, rescue_teams, drones; availability/status filters; case-study type/year |

V7 index names were checked against V1–V6 to avoid duplicates; common FK join
columns were already indexed by earlier migrations.

---

## PostgreSQL Deployment

Activate the production profile with the required environment variables:

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DB_HOST=localhost DB_PORT=5432 DB_NAME=disaster_db
export DB_USERNAME=disaster_app DB_PASSWORD='<strong-password>'
export JWT_SECRET='<base64-encoded-at-least-32-bytes>'   # REQUIRED - server fails fast if missing/weak
export CORS_ORIGINS='https://eoc.example.gov.in'
export RATE_LIMIT_MAX_PER_MINUTE=60
export RATE_LIMIT_PUBLIC_MAX_PER_MINUTE=20
export LOGIN_MAX_ATTEMPTS=5
export LOGIN_LOCKOUT_MINUTES=15
export FRONTEND_URL='https://eoc.example.gov.in'
export MAX_FILE_SIZE=5MB
export MAX_REQUEST_SIZE=10MB
# Optional providers (leave blank to use the bundled mocks):
export WEATHER_API_KEY=...
export MAIL_HOST=... MAIL_USERNAME=... MAIL_PASSWORD=...
export TWILIO_ACCOUNT_SID=... TWILIO_AUTH_TOKEN=... TWILIO_FROM_NUMBER=...

mvn -f backend/pom.xml spring-boot:run
```

Generate a compliant JWT secret:

```bash
# openssl is optional; any base64 string of >= 32 decoded bytes works
openssl rand -base64 48
```

**Single-instance note:** the rate limiter and lockout service are in-memory.
Scale the backend to one instance, or replace `RateLimitingFilter` /
`LoginAttemptService` with a shared store (e.g. Redis) before running a
multi-node cluster.

---

## Testing

```bash
cd backend
mvn clean test        # 112 tests: 61 integration + 51 unit
```

Coverage: auth & RBAC, AI engine, disaster lifecycle, emergency operations
center, real-time WebSocket, rescue-team coordination, admin dashboard, JWT,
rate limiting, account lockout, geospatial math, AI scoring.

Frontend production build:

```bash
cd frontend
npm run build         # Vite production bundle
```

---

## Operational Runbook

1. **Startup**: backend fails fast on weak/missing `JWT_SECRET`; the AI engine
   self-test runs and logs PASS/FAIL per check.
2. **Health**: `GET /actuator/health` (public); detailed info requires auth.
3. **Monitoring**: expose `/actuator/metrics` to your metrics collector.
4. **Backups**: standard PostgreSQL `pg_dump`; migrations are append-only via
   Flyway - never hand-edit an applied migration.
5. **WebSocket**: clients connect to `ws(s)://<host>/ws/live`; the frontend
   derives the host automatically (Vite dev proxy on port 5173 -> backend 8080).
