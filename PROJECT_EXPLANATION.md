# AI Disaster Management System v3.0 — Enterprise Command Center
## Complete Project Explanation Document (for College Review)

*Compiled from the actual source code. No code was modified during this analysis.*

---

## 1. PROJECT INTRODUCTION

### 1.1 Project Title
**AI Disaster Management System v3.0 — Enterprise Command Center**

### 1.2 Problem Statement
When a disaster strikes (flood, earthquake, cyclone, wildfire, etc.), response is fragmented:
- Citizens do not have a simple, anonymous way to report an incident.
- Authorities manage reports, resources, teams, hospitals and shelters in disconnected tools.
- Impact assessment (how many people are affected, how much damage, how urgent the response) is done manually and slowly.
- There is no real-time common operating picture that shows live locations of drones, teams and resources on one map.

### 1.3 Why This Problem Is Important
Disaster response is time-critical. Every minute of delay directly affects lives and property. A single platform that unifies reporting → triage → resource allocation → team dispatch → live monitoring → analytics reduces coordination time, removes information silos, and gives command centres a single source of truth.

### 1.4 Existing System Limitations
- Paper/SMS-based reporting with no central record.
- Spreadsheet-based resource tracking (no live availability, no movement trail).
- No priority/risk scoring — every incident gets treated equally.
- No real-time view of field assets (teams, drones, vehicles).
- No audit trail for accountability.
- No way for citizens to track the status of their own report.

### 1.5 Proposed Solution
A full-stack **Emergency Operations Center (EOC)** web platform where:
- Citizens report anonymously and track status via a public tracking ID.
- Authorized personnel (9 roles, 31 permissions) manage the full response lifecycle.
- A rule-based AI engine produces immediate impact/risk/priority estimates and recommends the nearest hospitals, shelters, volunteers and resource allocations.
- WebSocket pushes live updates (disaster, drone GPS, team locations) onto a Leaflet map.
- Everything is audit-logged, with analytics dashboards and CSV/PDF export.

### 1.6 Main Objectives
1. Provide anonymous, trackable disaster reporting for citizens.
2. Manage the disaster lifecycle (6 statuses) with full history.
3. Deliver instant, explainable AI analysis (damage, risk, priority, confidence, time estimates).
4. Recommend and optimize resources, hospitals, shelters, volunteers and evacuation plans.
5. Enable live monitoring through WebSocket + interactive maps.
6. Enforce role-based access, rate limiting, account lockout and audit logging.
7. Provide analytics, reporting and export for decision makers.

### 1.7 Real-World Applications
- Government Emergency Operations Centres
- District/State Disaster Management Authorities
- Civil Defence and Police response coordination
- NGO and humanitarian relief coordination
- Municipal command & control for floods/cyclones
- Campus/enterprise emergency response (demos)

---

## 2. COMPLETE SYSTEM OVERVIEW (End-to-End Working)

```
Citizen / User
     ↓
Public Report (anonymous)  OR  Register → Login (JWT + RBAC)
     ↓
Disaster Report Submitted  (type, severity, location, description, attachments)
     ↓
Backend Processing  (DisasterController → DisasterService)
     ↓
Database Storage    (disasters + status_timeline + audit_log + notification, H2/PostgreSQL via Flyway)
     ↓
AI Analysis Engine  (HeuristicPredictionModel + ScoreEngine + ConfidenceEngine + TimeEstimationEngine)
     ↓
Risk / Priority / Damage / Confidence / Response & Recovery Time estimates
     ↓
Resource Recommendation (nearest hospitals, shelters, volunteers, resource deficits, evacuation radius)
     ↓
Emergency Operations Center (admin dashboard, live map, priority queue)
     ↓
Rescue Team Assignment  (missions, members, vehicles, equipment, shifts)
     ↓
Live Monitoring  (WebSocket /ws/live → drone GPS, team locations, dashboard updates)
     ↓
Notifications   (in-app + SMS/email mocks, unread badge)
     ↓
Disaster Resolution  (6-status workflow, timeline per disaster)
     ↓
Analytics & Reports (12+ metrics, charts, CSV/PDF export, audit trail)
```

### Step-by-step detail

1. **Citizen reporting.** Anyone can open `/public-report` and file an anonymous report. The system returns a public report ID (`DMS-YYYYMMDD-XXXXXX`). The reporter later enters that ID on `/track-report` to follow the status and timeline. Alternatively, a registered user reports through `/report`.

2. **Backend processing.** `PublicDisasterController`/`DisasterController` validates the payload (Bean Validation) and hands it to `DisasterService`. The disaster is persisted with status `PENDING`, a `StatusTimeline` entry is written, an `AuditLog` entry is recorded, and a `Notification` is created.

3. **AI analysis.** The user (or system) calls the AI endpoints (`/api/predictions/analyze`, `/api/ai/recommend`). `AIEngineService` composes the prediction model and engines to return:
   - Damage level, % damage, affected population, economic loss (INR), casualty estimate, infrastructure impact, secondary hazards.
   - Risk score (0–100) and Priority score (0–100) with per-factor breakdown.
   - Confidence % with uncertainty and limitations.
   - Response-time and recovery-time estimates.
   - Ranked hospitals, shelters, volunteers, evacuation radius/instructions, and resource deficit/allocation plan.

4. **Command & control.** The Emergency Operations Center and Admin Dashboard show all incidents with filters, the priority/risk scores, and live map markers. Admin verifies the report (PENDING → VERIFIED), assigns teams, and dispatches resources through the 6-status workflow.

5. **Live monitoring.** The WebSocket channel (`/ws/live`) pushes `DISASTER`, `DRONE_LOCATION`, `LOCATION_SNAPSHOT`, `DASHBOARD` and `TEAM_LOCATION` events. A scheduler simulates drone GPS movement every few seconds; the map updates in place with moving markers, a heatmap and optional clustering.

6. **Notifications.** Every significant action generates an in-app notification (bell badge, notification center). SMS and email are mocked providers (log-only) that are ready to swap for Twilio/Spring Mail by filling config keys.

7. **Resolution.** Teams advance the disaster through `RESOURCES_DISPATCHED → IN_PROGRESS → RESOLVED`. Each transition is recorded on the timeline. Analytics recompute metrics; reports can be exported.

---

## 3. SYSTEM ARCHITECTURE

### 3.1 Architecture Diagram (description)

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Browser)                        │
│   React 18 + Vite + Material UI + React Router + Recharts +      │
│   Leaflet + Axios + STOMP/SockJS client                          │
│   AuthContext │ WebSocketContext │ ThemeContext                  │
└───────────────┬──────────────────────────────────┬──────────────┘
                │  REST (JSON, /api/*, JWT Bearer)  │  WebSocket (/ws/live)
                ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BACKEND (Spring Boot 3.2)                 │
│   Controller Layer (28 REST controllers)                         │
│   Service Layer (43 services)                                    │
│   AI Engine (HeuristicPredictionModel, ScoreEngine, Confidence,  │
│              TimeEstimation, 5 recommendation engines)           │
│   Repository Layer (26 Spring Data JPA repositories)             │
│   Security (JwtAuthFilter, RateLimitingFilter, RbacService,      │
│             LoginAttemptService, AuditService)                   │
│   WebSocket (WebSocketConfig → /ws/live broadcast)               │
└──────────────────────────────┬───────────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│              DATABASE (H2 in-memory dev / PostgreSQL prod)       │
│   Flyway migrations V1–V7, V9 → 25+ tables                       │
│   DataSeeder (demo data, dev profile only)                       │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Frontend
- **Framework:** React 18 (Vite 5 build tool), JavaScript (JSX).
- **UI library:** Material UI (MUI) with a custom Deep Blue (`#0F4C81`) + Teal + Orange EOC theme, dark/light mode via `ThemeContext`.
- **Pages (29):** Login, Register, Forgot/Reset Password, PublicReport, PublicTrack, TrackReport, UserDashboard, AdminDashboard, ReportDisaster, DisasterList, DisasterDetail, AIInsights, MapPage, DroneMonitoring, HospitalManagement, ShelterManagement, VolunteerManagement, ResourceManagement, NotificationCenter, AuditLogPage, AdvancedAnalytics, UserManagement, RescueTeamManagement, RouteOptimization, CaseStudyAnalysis, AdminControlCenter, EmergencyOperationsCenter, UserProfile.
- **Components (22):** Sidebar, ProtectedRoute, LiveMap, MapView, NotificationBell, StatCard, AnimatedCounter, StatusBadge, Breadcrumbs, FileUploadDropzone, ErrorBoundary, GlassPanel, EmptyState, LoadingSkeleton, WebSocketStatus, ChartCard, PageHeader, SectionCard, AuthLayout, ThemeToggle, Footer.
- **Contexts (3):** `AuthContext` (user, login/logout, permissions), `WebSocketContext` (reconnect/backoff, live events), `ThemeContext`.
- **UI features:** lazy code-splitting, route-level error boundaries, 404/403 pages, loading skeletons, animated counters, breadcrumbs, toasts, charts (Recharts), interactive map (Leaflet with clustering, heatmap, 3 layer types), CSV/PDF export.

### 3.3 Backend
- **Framework:** Spring Boot 3.2.0 (Java 17), Maven.
- **API architecture:** REST with JSON; `@RestController` + DTOs; Bean Validation; global exception handler.
- **Controllers (28):** Auth, User, Disaster, PublicDisaster, Prediction, AIEngine, ResourceRecommendation, Route, Analytics, Monitoring, Location, Drone, Hospital, Shelter, Volunteer, Resource, RescueTeam, Mission, Shift, Vehicle, Equipment, Notification, AuditLog, CaseStudy, Weather, Export, Timeline, Admin.
- **Services (43):** domain services (e.g. `AuthService`, `DisasterService`, `ResourceService`, `RescueTeamService`, `MissionServiceImpl`, `ShiftServiceImpl`, `HospitalService`, `ShelterService`, `VolunteerService`, `DroneService`, `RouteOptimizationService`, `AnalyticsService`, `ExportService`, `AuditService`, `WeatherService`) plus the AI package services.
- **Security implementation:** stateless JWT filter chain, rate limiting filter, RBAC via `@PreAuthorize("@rbacService.hasPermission(...)")`, BCrypt, audit logging.

### 3.4 Database
- **Database:** H2 in-memory (default, PostgreSQL compatibility mode) / PostgreSQL (production profile).
- **Tables/entities:** 29 entities mapped to 25+ tables (see §8).
- **Relationships:** child→parent `@ManyToOne` (LAZY) throughout; collections accessed through repositories; optimistic locking (`@Version`) on User, Disaster, Resource, RescueTeam, Vehicle, Equipment, TeamShift; soft delete on Disaster.
- **Data flow:** Controller → Service → Repository → DB. Flyway runs schema migrations on startup; `DataSeeder` populates demo rows (dev profile only).

### 3.5 Communication
- **REST APIs:** all CRUD and action endpoints under `/api/*`, JSON payloads, JWT bearer auth, HATEOAS-style DTOs.
- **WebSocket:** Spring WebSocket at `/ws/live` (application-level PING/PONG keepalive, stale-session cleanup). `WebSocketConfig.broadcastUpdate(type, data)` sends `LiveUpdateDTO {type, data, timestamp}` to all sessions. Types: `DISASTER`, `DRONE_LOCATION`, `LOCATION_SNAPSHOT`, `DASHBOARD`, `TEAM_LOCATION`.
- **Real-time updates:** a scheduler (`LiveSimulationScheduler`) emits simulated drone GPS and location snapshots every few seconds; disaster status changes also trigger broadcasts, so dashboards and maps update without page refresh.

---

## 4. TECHNOLOGY STACK

| Technology | Purpose | Why Used |
|---|---|---|
| **Backend** | | |
| Java 17 | Programming language | LTS, modern language features (records, switch expressions, sealed types) |
| Spring Boot 3.2 | Application framework | Auto-configuration, embedded Tomcat, mature ecosystem |
| Maven | Build & dependency management | Standard, reproducible builds, easy profile handling |
| Spring Security | Authentication/authorization | Industry-standard security filters, method security |
| JWT (jjwt 0.12.3) | Stateless access/refresh tokens | Scalable, no server-side session storage |
| Spring Data JPA / Hibernate | ORM | Object-relational mapping, repository abstraction |
| Flyway | Database versioning | Deterministic, versioned schema migrations |
| Spring WebSocket | Real-time push | Live map/drone/dashboard updates |
| Spring Validation | Request validation | Bean Validation annotations on DTOs |
| SpringDoc/OpenAPI | API documentation | Interactive Swagger UI at `/swagger-ui.html` |
| H2 / PostgreSQL | Database | H2 zero-config demo DB; PostgreSQL production profile |
| **Frontend** | | |
| React 18 | UI framework | Component model, hooks, huge ecosystem |
| Vite 5 | Build tool/dev server | Fast HMR, lightweight bundling |
| Material UI (MUI) | Component library | Professional admin/command-center look, dark mode |
| Axios | HTTP client | Interceptors, token refresh queue |
| React Router 6 | Client routing | Protected/permission-gated routes, lazy loading |
| Recharts | Charts | Dashboard/analytics visualizations |
| Leaflet + react-leaflet | Maps | Lightweight interactive maps, markers, heatmap |
| jsPDF / PapaParse | Export | PDF and CSV generation |
| STOMP/SockJS client | WebSocket client | Live updates (used via the WebSocket context) |
| **Other** | | |
| Git | Version control | Project history, collaboration |

---

## 5. MODULE-WISE EXPLANATION

### 5.1 Authentication Module
Implemented in `AuthController` + `AuthService` + `security/` package.

- **Registration:** public `POST /api/auth/register` → creates user with BCrypt-hashed password, generates a 24-hour verification token, sends a mock email.
- **Email verification:** `POST /api/auth/verify-email` validates the token and marks the account verified.
- **Login:** `POST /api/auth/login` → `AuthenticationManager` validates credentials, checks lockout/verification/deactivation, issues a 24h HS256 access token (`JwtUtils`) and a 7-day DB-backed refresh token.
- **Refresh tokens:** `POST /api/auth/refresh` rotates the refresh token (each refresh invalidates the previous one); stored in `refresh_tokens` table.
- **Logout:** `POST /api/auth/logout` revokes the refresh token.
- **Forgot/reset password:** `POST /api/auth/forgot-password` (no user enumeration — always returns the same message) → 1-hour reset token → `POST /api/auth/reset-password`.
- **Role-based access control:** login response includes the user's permissions; `RbacService` (9 roles × 31 permissions) is used both on the backend (`@PreAuthorize`) and the frontend (`ProtectedRoute`, permission-filtered sidebar).

### 5.2 Disaster Reporting Module
Implemented in `DisasterController`, `PublicDisasterController`, `DisasterService`.

- **Public reporting:** anonymous form at `/public-report`; produces a trackable `DMS-YYYYMMDD-XXXXXX` report ID; status trackable at `/track-report` without login.
- **User reporting:** authenticated `POST /api/disasters`.
- **Disaster types (8):** Flood, Earthquake, Cyclone, Wildfire, Tsunami, Landslide, Drought, Epidemic.
- **Severity (4):** Low, Medium, High, Critical.
- **Attachments:** up to 5 files, 5 MB each, MIME allowlist, stored as data-URLs (`FileUploadDropzone` + V4 migration).
- **Tracking:** report ID lookup + status timeline; public feed strips reporter PII.

### 5.3 Disaster Management Module
- **Lifecycle (6 statuses):** `PENDING → VERIFIED → ASSIGNED → RESOURCES_DISPATCHED → IN_PROGRESS → RESOLVED`. `DisasterService.getStatusFlow()` exposes the allowed transitions for workflow UIs; frontend `STATUS_TRANSITIONS` mirrors it.
- **Status changes:** `PUT /api/disasters/{id}/status` validates the transition and records a `StatusTimeline` entry.
- **Comments:** add/list/delete threads on a disaster (delete allowed for owner or ADMIN).
- **Assignments:** `disaster_assignments` history (who was assigned and when).
- **Priority:** manual `LOW/MEDIUM/HIGH/CRITICAL` priority per disaster.
- **Search/filter/pagination:** JPA Specifications over search, type, severity, status, priority, source; sortable, paginated.
- **Soft delete:** disasters are soft-deleted (deleted_at flag) instead of hard-removed.

### 5.4 AI Intelligence Module
Implemented in `com.disaster.ai` package: `AIEngineService` (facade), `HeuristicPredictionModel` (the `PredictionModel` implementation), `ScoreEngine`, `ConfidenceEngine`, `TimeEstimationEngine`, and 5 recommendation engines (Hospital, Shelter, Volunteer, Evacuation, ResourceOptimization). Exposed via `PredictionController`, `AIEngineController`, `ResourceRecommendationController`.

- **Damage prediction:** per-type damage-level table (e.g. Tsunami → Catastrophic at High severity), per-type base population, per-capita economic loss (INR), casualty rate, and infrastructure impact. A stable coordinate hash adds deterministic geographic variance (same inputs → same outputs; no `Math.random()`).
- **Risk scoring (0–100):** weighted 30% hazard likelihood + 25% exposure + 20% vulnerability + 15% coping capacity + 10% weather amplification, plus a 12% weather-alert boost.
- **Priority calculation (0–100):** weighted 30% severity + 20% population + 15% damage + 10% urgency + 10% infrastructure fragility + 15% local readiness. Output is labelled CRITICAL/HIGH/MEDIUM/LOW.
- **Confidence calculation:** rubric combining input completeness (40%), model coverage (30%), data quality (20%), historical basis (10%) → overall %, uncertainty %, methodology and limitations list.
- **Response/recovery time:** baseline-anchored arithmetic (base time ÷ urgency factor × access factor) using readiness and confidence.
- **Resource optimization:** per-disaster-type resource needs vs. current inventory → status per type (e.g. CRITICAL deficit) and an allocation plan.
- **Recommendation engine:** ranks the 5 nearest hospitals (beds, distance), 5 nearest shelters (available space), matched volunteers (skill + proximity), builds an evacuation plan (danger radius + instructions by disaster type), and produces a plain-language summary.
- **Honest statement:** this is a **deterministic rule-based/heuristic engine**, not machine learning. It is explainable (every score lists its factor weights/contributions), fully offline, and tested via a built-in self-test (`GET /api/ai/self-test`, 15 checks). The `PredictionModel` interface is the documented swap point for a real ML model.
- **Future upgrade:** train a model (e.g. LightGBM / small NN) on historical disaster→impact data, serve it via ONNX Runtime or DJI inside the same service, and register it as `@Primary` — controllers and orchestration stay unchanged.

### 5.5 Resource Management Module
`ResourceController`, `ResourceService`, `Resource` + `ResourceMovement`.

- **Inventory:** full CRUD for resources (type, quantity, condition, location, deployment status).
- **Resource allocation:** availability tracking, assignment to disasters/missions.
- **Movement tracking:** every deploy/return/maintenance action writes a `ResourceMovement` record (audit trail of where each resource went).

### 5.6 Rescue Team Management Module
`RescueTeamController`, `MissionController`, `ShiftController`, `VehicleController`, `EquipmentController` + services; the largest frontend page (`RescueTeamManagement.jsx`, ~1,167 lines).

- **Teams:** CRUD, status, leaders.
- **Members:** team members with roles and availability; live location updates (`TeamLocationUpdate`).
- **Vehicles:** vehicle registry with type/status.
- **Equipment:** equipment inventory per team with condition/status.
- **Missions:** `RescueMission` CRUD, status transitions, `MissionEvent` history log.
- **Shift scheduling:** `TeamShift` roster by date/range with per-member shift status.

### 5.7 Hospital and Shelter Management Module
`HospitalController`/`HospitalService`, `ShelterController`/`ShelterService`.

- **Hospitals:** name, address, available beds, ICU beds, doctors available, blood bank availability, emergency contact, geo-coordinates.
- **Shelters:** name, capacity, current occupancy, available space, address, geo-coordinates, amenities.
- Both are geo-anchored so the AI recommendation engines can rank them by Haversine distance.

### 5.8 Drone Monitoring Module
`DroneController` + `DroneService` + `LiveSimulationScheduler`.

- **Drone management:** CRUD, model, battery level, status (enum), assigned region.
- **GPS simulation:** scheduler moves each active drone by a random step (0.8–2.4 km) every few seconds and broadcasts `DRONE_LOCATION` over WebSocket.
- **Live tracking:** `DroneMonitoring` page shows drones moving on the map with battery and status; markers update in place.

### 5.9 Live Monitoring Module
`WebSocketConfig` + `WebSocketSessionService` + `LiveTrackingService` + `LiveSimulationScheduler`.

- **WebSocket:** raw Spring WebSocket at `/ws/live`; application-level PING/PONG keepalive; stale-session cleanup; `LiveUpdateDTO` broadcast envelope.
- **Real-time dashboard:** `EmergencyOperationsCenter.jsx` (811 lines) renders a live map, stats, and an operations table that update via WebSocket.
- **Maps/heatmaps/markers:** `LiveMap.jsx` custom clustering and heatmap (hand-rolled on Leaflet), color-coded markers by severity/status, live marker animation, 3 map layers (street/terrain/satellite).

### 5.10 Notification System Module
`NotificationController` + `NotificationServiceImpl` + `NotificationBell`.

- **In-app notifications:** created on key events, unread badge (30-second poll), notification center, mark read/all.
- **Email/SMS:** `EmailProvider`/`SMSProvider` interfaces with `MockEmailProvider`/`MockSMSProvider` log-only implementations. Config keys exist for Spring Mail (`spring.mail.*`) and Twilio (`sms.twilio.*`) — fill them to go live.

### 5.11 Analytics Module
`AnalyticsController` + `AnalyticsService` + `ExportController` + `ExportService`.

- **Dashboard:** 12+ metrics, 4 chart types (bar/pie/area/line/radar), utilization rates, animated counters (`AdvancedAnalytics.jsx`, `UserDashboard`, `AdminDashboard`).
- **Reports/export:** CSV (with BOM for Excel), PDF via OpenPDF; "Excel" export currently produces CSV.

---

## 6. USER ROLES AND PERMISSIONS

The system defines **9 roles** (`Role` enum) mapped to **31 permissions** (`Permissions` + `RbacService`). Three-level enforcement: Spring Security method annotations (backend) + permission strings on routes (frontend) + permission-filtered sidebar items.

| Role | What it can do (permission highlights) |
|---|---|
| **ADMIN** (`admin/admin123`) | All 31 permissions — everything. |
| **DMO** (Disaster Management Officer, `dmo/dmo123`) | Full disaster management (create/update/delete), user view, AI, resources manage, rescue team manage, analytics, export; no user management, no audit. |
| **POLICE** (`police/police123`) | View dashboard/map/AI/disasters, create & update disasters, view resources and rescue teams. |
| **FIRE_DEPARTMENT** (`fire/fire123`) | Same scope as Police (law-enforcement-style access for fire response). |
| **HOSPITAL_STAFF** (`hospital/hospital123`) | View disasters, manage hospitals (beds, ICU, blood bank), view resources/teams. |
| **RESCUE_TEAM** (`rescue/rescue123`) | View disasters, view rescue teams/resources/drones, use route optimization. |
| **VOLUNTEER_COORDINATOR** (`volunteer/volunteer123`) | Manage volunteers, view shelters and disasters. |
| **NGO_COORDINATOR** (`ngo/ngo123`) | Manage shelters, view volunteers and disasters. |
| **USER** (citizen, `user/user123`) | Dashboard, profile, notifications, map, AI, route, case studies, view & create disasters. |

Public users (no account) can file anonymous reports and track them via report ID.

---

## 7. COMPLETE API WORKING

### 7.1 How the frontend talks to the backend
- Axios instance (`baseURL /api`) with a request interceptor that injects `Authorization: Bearer <token>`.
- A response interceptor implements **single-flight token refresh**: on a 401 (that isn't an auth endpoint), it queues concurrent requests, refreshes the access token once via `/auth/refresh`, replays the queue, and only redirects to `/login` if refresh fails.
- Vite dev server proxies `/api` to `localhost:8080`.
- The WebSocket context opens `/ws/live` and reconnects with exponential backoff.

### 7.2 Example — Login API
```
POST /api/auth/login   {username, password}
   → AuthController.login (@Valid LoginRequest)
   → AuthService.login: AuthenticationManager.authenticate
        → UserDetailsServiceImpl.loadUserByUsername
        → LoginAttemptService (failed/attempt check, lockout)
        → BCrypt check by Spring Security
   → JwtUtils generates access token (24h)
   → RefreshToken created & stored (7 days, rotation)
   → Response JwtResponse {token, refreshToken, user {id, username, role, permissions}}
   → Frontend AuthContext stores user (localStorage if "remember me", else sessionStorage)
```

### 7.3 Example — Disaster Creation API
```
POST /api/disasters   {type, severity, location, lat, lng, description, ...}
   → JwtAuthFilter validates JWT → Authentication principal
   → @PreAuthorize("@rbacService.hasPermission(..., 'DISASTER_CREATE')")
   → DisasterController.createDisaster → DisasterService.createDisaster
        → Bean Validation on DisasterRequest
        → Save Disaster (PENDING) + StatusTimeline entry
        → AuditService.record(...) → audit_logs
        → NotificationService.notify(...) → notifications + WebSocket DISASTER broadcast
   → Response DisasterResponse
```

---

## 8. DATABASE EXPLANATION

**Database used:** H2 in-memory (default, `MODE=PostgreSQL`, seeded) / PostgreSQL (production profile). `ddl-auto=validate` — schema is owned entirely by Flyway.

**Flyway migrations:** V1 (init schema), V2 (public-report fields), V3 (enrichment + comments/assignments), V4 (attachments), V5 (rescue-team coordination), V6 (mission assets + resource movements), V7 (performance indexes), V9 (optimistic locking + soft delete). *(V8 is intentionally absent.)*

**Major entities and relationships:**

| Entity | Table | Key fields | Relationship |
|---|---|---|---|
| `User` | users | username, email, password(BCrypt), role, verified, enabled, version | parent of disasters, notifications, tokens |
| `Disaster` | disasters | type, severity, status, priority, location, lat/lng, description, date, deleted_at, version | → User (ManyToOne); children: comments, attachments, assignments, status_timeline |
| `Resource` | resources | type, quantity, condition, location, status, version | → ResourceMovement (children) |
| `ResourceMovement` | resource_movements | movement type (DEPLOY/RETURN/MAINTENANCE), timestamps | → Resource (ManyToOne) |
| `Hospital` | hospitals | beds, ICU beds, doctors, blood bank, geo | AI-ranked by distance |
| `Shelter` | shelters | capacity, occupancy, available space, geo | AI-ranked by distance |
| `Volunteer` | volunteers | skills, availability, deployment, geo | matched by skill + proximity |
| `Drone` | drones | model, battery, status, geo | live simulation |
| `RescueTeam` | rescue_teams | name, status, leader, version | → TeamMember/Vehicle/Equipment (children) |
| `RescueMission` | rescue_missions | status, description | → MissionEvent (children) |
| `Notification` | notifications | message, read flag, type | → User (ManyToOne) |
| `AuditLog` | audit_logs | actor, action, target, timestamp | flat log table |
| `StatusTimeline` | status_timeline | from → to status, changed_by, timestamp | → Disaster (ManyToOne) |

Other tables: `disaster_comments`, `disaster_attachments`, `disaster_assignments`, `team_members`, `rescue_vehicles`, `rescue_equipment`, `mission_events`, `team_location_updates`, `team_shifts`, `refresh_tokens`, `verification_tokens`, `case_studies`, `system_settings`.

**Relationships:** all child→parent are `@ManyToOne(LAZY)`; there are no `@OneToMany` collections in entities (children are fetched via repositories). Optimistic locking via `@Version` on high-concurrency entities. Soft delete on disasters.

**Sample data:** 9 demo users (one per role), 4 disasters (Flood/Mumbai IN_PROGRESS, Earthquake/Guwahati ASSIGNED, Cyclone/Chennai PENDING, Wildfire/Dehradun RESOLVED), 3 hospitals, 3 shelters, 3 volunteers, 5 resources, 3 drones, 3 rescue teams, 3 team members, 2 case studies, 3 system settings — all seeded by `DataSeeder` on the non-postgres profile.

---

## 9. SECURITY FEATURES

| Feature | Implementation |
|---|---|
| **JWT authentication** | HS256, 24h access token; stateless (`SessionCreationPolicy.STATELESS`); `JwtAuthFilter` on every request; production fails fast on a missing/weak secret |
| **BCrypt encryption** | `BCryptPasswordEncoder` for all stored passwords |
| **Role-based authorization** | 9 roles × 31 permissions; `@PreAuthorize` on controllers + `RbacService`; `/api/audit-logs/**` and `/api/admin/**` are ADMIN-only at URL level |
| **Rate limiting** | `RateLimitingFilter`: 60 req/min for authenticated, 20/min for public, per-IP, returns 429 |
| **Account lockout** | `LoginAttemptService`: 5 failed logins → 15-minute lockout |
| **Audit logging** | `AuditService` records register/login/failures, disaster CRUD/status changes, assignments, resource movements |
| **CORS** | Explicit allowed origins (`http://localhost:5173` by default), methods GET/POST/PUT/DELETE/OPTIONS, credentials, `Content-Disposition` exposed |
| **CSP / security headers** | Content-Security-Policy (self + inline scripts/styles, WebSocket `connect-src ws: wss:`), HSTS (31536000s), X-Content-Type-Options, Referrer-Policy, Permissions-Policy (camera/mic blocked), X-Frame-Options same-origin |
| **PII redaction** | Public disaster feed strips reporter PII before broadcasting |
| **Other** | 5-file/5-MB upload limits with MIME allowlist, soft delete + optimistic locking, deactivated-account rejection at login |

---

## 10. AI EXPLANATION FOR REVIEWER (simple version)

> **"What AI is used in this project?"**

**Current (implemented):** The project ships an **explainable rule-based "AI" engine** — not machine learning. It works like a medical triage decision-tree combined with math:

1. **Lookup tables** — each disaster type has calibrated baselines (e.g. a Tsunami affects more people and causes more damage than a Landslide).
2. **Weighted scoring formulas** — Priority = 30% severity + 20% population + 15% damage + 10% urgency + 10% fragility + 15% readiness → a 0–100 score with a CRITICAL/HIGH/MEDIUM/LOW label.
3. **Heuristic prediction** — damage %, affected population, economic loss (INR), casualties, response/recovery time windows computed from those baselines, scaled by severity and infrastructure factor.
4. **Geospatial calculations** — Haversine distance ranks the nearest hospitals, shelters and volunteers; evacuation radius depends on disaster type.
5. **Confidence + explainability** — every score reports its factor breakdown and limitations, and there is a built-in 15-check self-test.

Because it's deterministic (same inputs → same outputs, coordinate-hashed, no `Math.random()`), it is reproducible and easy to explain — which is why it suits a college review.

**Future (roadmap):** replace `HeuristicPredictionModel` with a real ML model (the `PredictionModel` interface is already the swap point) trained on historical disaster datasets (LightGBM/XGBoost or a small neural network served via ONNX Runtime/DJL), add satellite-image analysis (damage detection from imagery), integrate live weather API data, IoT sensor streams, and real drone hardware for live telemetry.

---

## 11. COMPLETE DEMO SCRIPT

> Pre-requisite: `cd backend && mvn spring-boot:run` and `cd frontend && npm install && npm run dev`. Open `http://localhost:5173`.

| Step | Action | What the reviewer sees |
|---|---|---|
| 1 | Open the application | Login screen, professional EOC theme |
| 2 | Login as **admin / admin123** | Landing on dashboard; sidebar shows all modules |
| 3 | Show dashboard | Stats cards, animated counters, recent disasters, charts |
| 4 | Role contrast | Logout → login as **user/user123** → sidebar shrinks; login as **police** etc. (proves RBAC) |
| 5 | Public report | Open `/public-report`, submit a sample flood report → note the `DMS-...` ID |
| 6 | Track report | `/track-report`, enter the ID → status/timeline visible anonymously |
| 7 | Create disaster report | As admin, `/report` → add a disaster with attachments |
| 8 | AI analysis | Open the disaster → run **Analyze** → show damage %, risk, priority, confidence, response/recovery time with factor breakdowns |
| 9 | AI recommendations | Run **Recommend** → ranked hospitals, shelters, volunteers, resource deficit table, evacuation radius |
| 10 | Disaster workflow | Advance status PENDING→VERIFIED→ASSIGNED→RESOURCES_DISPATCHED→IN_PROGRESS→RESOLVED, show timeline + comments |
| 11 | Rescue teams | `/rescue-teams` → create team, assign members/vehicles/equipment, create mission and shifts |
| 12 | Live map | `/map` or Emergency Operations Center → live drone markers moving, heatmap, layers |
| 13 | Drone monitoring | `/drones` → battery/status, simulated GPS movement on map |
| 14 | Route optimization | `/route-optimization` → multi-waypoint route with distance/time |
| 15 | Analytics | `/analytics` → 12+ metrics, multiple chart types, utilization rates |
| 16 | Notifications | Show unread badge, notification center, mark-as-read |
| 17 | Export | Export disasters as CSV/PDF; show audit log at `/audit-logs` |
| 18 | Admin control center | `/admin-control` → settings, quick actions |
| 19 | Swagger | `http://localhost:8080/swagger-ui.html` → API documentation |

**Most impressive screens:** Emergency Operations Center (live), AI Insights (score breakdowns), Live Map with moving drones, Rescue Team Management, Admin Control Center, Analytics, Audit Logs.

---

## 12. REVIEWER QUESTIONS AND ANSWERS

**Q1. Why did you choose this technology stack?**
Spring Boot for rapid REST/WebSocket development, mature security and ecosystem; React + MUI for a professional, permission-aware single-page UI; H2 for a zero-config demo and PostgreSQL for production; Flyway so schema changes are versioned and reproducible.

**Q2. Where is AI actually used?**
In the AI module: damage prediction, affected-population and economic-loss estimates, risk score, priority score, confidence %, response/recovery time, and recommendations (nearest hospitals/shelters/volunteers, resource deficits, evacuation plan). It is rule-based/heuristic and fully explainable (each score lists its factors).

**Q3. How does prediction work?**
Lookup tables per disaster type (baseline population, per-capita loss, casualty rate, damage-level matrix) scaled by severity and infrastructure factor, with a stable coordinate hash for geographic variance — deterministic and reproducible.

**Q4. Why Spring Boot and not something else?**
Auto-configuration speeds development; embedded server simplifies deployment; Spring Security integrates cleanly; the same app runs H2 (demo) or PostgreSQL (production) by switching a profile.

**Q5. Why React?**
Component reusability (22 shared components), hooks for contexts (auth/websocket/theme), fast HMR with Vite, and a large ecosystem (MUI, Recharts, Leaflet).

**Q6. How is security handled?**
JWT (HS256, 24h) + rotating refresh tokens (7d), BCrypt, 9-role/31-permission RBAC, rate limiting (60/min, 20/min public), account lockout (5 fails → 15 min), audit logging, CORS allowlist, CSP/HSTS headers, PII redaction on public feeds.

**Q7. How is real-time tracking achieved?**
Spring WebSocket at `/ws/live`; a scheduler simulates drone GPS every few seconds and broadcasts `DRONE_LOCATION`/`LOCATION_SNAPSHOT`/`DASHBOARD` events; the frontend WebSocket context reconnects with backoff and updates the map/dashboard in place.

**Q8. What are the limitations?**
The "AI" is rule-based, not machine-learned; SMS/email/weather are mocks (ready to swap via config keys); route optimization doesn't reorder waypoints; rate limiting is in-memory (single instance); no Docker/CI; one schema-validation mismatch currently breaks the integration tests (see §14).

**Q9. What future improvements are possible?**
Real ML model, satellite-image damage detection, live weather API, IoT sensors, real drone hardware, cloud deployment, mobile app, AI chatbot, shortest-path/TSP route optimization.

**Q10. Is there a database with relationships?**
Yes — 29 entities, 25+ tables (users, disasters, resources, hospitals, shelters, volunteers, drones, rescue teams/missions/shifts, notifications, audit logs, tokens, etc.), child→parent `@ManyToOne` relationships, Flyway versioned migrations, and seeded demo data.

**Q11. How does the frontend know which menus to show?**
The login response includes the user's permission list; the `Sidebar` renders only permitted items, `ProtectedRoute` guards each route with a permission string, and the backend enforces the same permission via `@PreAuthorize`.

**Q12. Where is the project deployed / how do I run it?**
`backend`: `mvn spring-boot:run` (H2 auto-seeded, Swagger at `/swagger-ui.html`). `frontend`: `npm run dev` (Vite proxy → 8080). Demo logins: `admin/admin123`, `user/user123`, plus one per role.

---

## 13. FUTURE ENHANCEMENTS (industry-level)

| Enhancement | Description | Priority |
|---|---|---|
| Real ML model | Train on historical disaster→impact datasets; serve via ONNX/DJL behind the existing `PredictionModel` interface | High |
| Satellite image analysis | Damage detection/classification from satellite/aerial imagery (deep learning) | Medium |
| Weather API integration | Replace `MockWeatherProvider` with OpenWeatherMap (key field already configured) | Medium |
| IoT sensors | Real-time river level / seismic / air-quality sensors feeding predictions | Medium |
| Real drone hardware | Replace GPS simulation with live telemetry via drone SDK/MAVLink | Medium |
| Cloud deployment | Docker + docker-compose, CI/CD, containerized DB | High |
| Mobile application | PWA / native app for field teams | Medium |
| AI chatbot | Natural-language query of incidents/resources for operators | Low |
| Route optimization | Nearest-neighbor/TSP ordering, road-network routing (OSRM/GraphHopper) | Medium |
| Real SMS/email | Twilio + Spring Mail (config already in place) | Low |
| Multi-node rate limiting | Redis-backed rate limiting/lockout | Medium |

---

## 14. FINAL PRESENTATION SCRIPTS

### 5-Minute Script
> "This is the **AI Disaster Management System** — an Emergency Operations Center web platform built with **Spring Boot 3.2** and **React 18**.
>
> The problem: disaster response today is fragmented — reports, resources and teams are handled in separate tools, impact assessment is manual, and there's no live common operating picture.
>
> Our solution is one platform covering the whole lifecycle. A **citizen** can file an anonymous report and track it with a public ID. The **backend** validates and stores it, then our **AI engine** — a deterministic, explainable rule-based model — instantly estimates damage percentage, affected population, economic loss, casualties, a 0–100 **risk score** and **priority score**, and a **confidence** level. It also recommends the nearest **hospitals, shelters, volunteers** and flags **resource deficits** using Haversine distance.
>
> The **Emergency Operations Center** gives admins a live map where **drones are simulated in real time over WebSocket** (`/ws/live`), alongside rescue teams, missions, shift scheduling and vehicles. Security is enterprise-grade: **JWT with rotating refresh tokens, 9 roles and 31 permissions, BCrypt, rate limiting, account lockout, audit logging, and CSP headers**.
>
> Everything is persisted in H2 for the demo (PostgreSQL for production) using **Flyway migrations** across 25+ tables with seed data. Finally, **analytics dashboards** and **CSV/PDF export** close the loop.
>
> Honest note: the 'AI' today is a **rule-based heuristic engine** behind a swappable `PredictionModel` interface — the next step is swapping in a trained ML model. One known issue: a schema-validation mismatch currently fails the integration tests, which I'll fix. Overall the project is feature-complete across the full disaster-response workflow."

### 10-Minute Script
> *(Everything in the 5-minute script, expanded with a live demo.)*
>
> "Let me show it live. [Open app] I'll log in as **admin**. Notice the dashboard — animated counters and charts. If I log out and in as **user**, the sidebar shrinks — that's RBAC enforced on both frontend and backend.
>
> [Public flow] Here's the public report form — I'll submit an anonymous flood report and get a `DMS-...` tracking ID. On the track page, anyone can follow its status and timeline without an account.
>
> [Disaster + AI] Now, as admin, I'll open a disaster and run the AI **Analyze** — you see damage %, affected population, economic loss, risk and priority scores with their **factor breakdowns**, confidence with uncertainty, and response/recovery time. Then **Recommend** — nearest hospitals sorted by beds and distance, shelters, matched volunteers, resource deficit table, and evacuation radius. This is all deterministic and explainable.
>
> [Workflow] I'll walk the disaster through all six statuses and show the timeline and comments.
>
> [Ops] Next, rescue teams — create a team, add members, vehicles, equipment, a mission and shifts. Then the **live map** — watch the drone markers move via WebSocket without a page refresh, plus heatmap and street/terrain/satellite layers. Route optimization calculates multi-waypoint distance and time.
>
> [Analytics & security] Finally, the analytics page with 12+ metrics and charts, CSV/PDF export, the audit log, and the admin control center. All APIs are documented in Swagger.
>
> To summarize — the project is a complete, full-stack EOC: anonymous reporting, AI-powered triage and recommendations, live monitoring, RBAC security, and analytics. The AI is currently a transparent rule-based engine with a clean path to real ML, and the remaining item is fixing one schema-validation issue that fails the integration tests. Thank you."

---

## APPENDIX A — KNOWN ISSUE (transparency for reviewers)
A schema-validation mismatch exists between Flyway migration `V4__disaster_attachments.sql` (creates `data_url TEXT`) and the `DisasterAttachment` entity (`@Lob @Column(columnDefinition = "CLOB")`). With `ddl-auto=validate`, the Spring context fails to start, which currently causes **all integration tests** to fail (unit tests pass). The fix is to align the migration column type with the entity (or drop `@Lob`), plus a reconciliation migration. **Fix this before the review** — it does not affect the running app once the column type is aligned, but `mvn test` must be green for the review.

## APPENDIX B — QUICK FACTS
- Roles: 9 · Permissions: 31 · Controllers: 28 · Services: 43 · Repositories: 26 · Entities: 29
- Frontend pages: 29 · Components: 22 · Contexts: 3
- Disaster types: 8 · Severities: 4 · Statuses: 6 · Attachments: ≤5 files, ≤5 MB each
- Token lifetimes: access 24 h · refresh 7 days (rotating)
- Rate limits: 60/min auth, 20/min public · Lockout: 5 attempts / 15 min
- DB: H2 (demo) / PostgreSQL (prod) · Flyway V1–V7, V9
- Demo logins: `admin/admin123`, `user/user123`, `dmo/dmo123`, `police/police123`, `fire/fire123`, `hospital/hospital123`, `rescue/rescue123`, `volunteer/volunteer123`, `ngo/ngo123`
