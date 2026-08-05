# Architecture Diagram — AI Disaster Management System

> Rendered as a Mermaid `flowchart`. Describes the logical 3-tier architecture plus
> cross-cutting concerns (security, monitoring, messaging).

```mermaid
flowchart TB
    subgraph Client[Client Tier]
        Browser["Browser (React SPA)"]
        Browser --> MUI["Material UI Command Center"]
        Browser --> Maps["Leaflet / OpenStreetMap"]
        Browser --> Charts["Recharts Analytics"]
        Browser --> WSC["STOMP/SockJS WebSocket Client"]
        Browser --> AuthUI["Auth Flows (Login / Register / Reset)"]
    end

    subgraph Edge[Edge / API Gateway]
        ViteProxy["Vite Dev Proxy (:5173) / Reverse Proxy"]
        RateLimit["Rate Limiting Filter"]
        CORS["CORS Configuration"]
        Headers["Security Headers (CSP, HSTS, X-Frame, Referrer, Permissions-Policy)"]
    end

    subgraph API[API Tier — Spring Boot 3.2 (:8080)]
        direction TB
        Security["Spring Security Filter Chain<br/>JwtAuthFilter | Method Security (RBAC)"]
        Controllers["REST Controllers<br/>Auth, Disaster, User, Admin, AI, Prediction,<br/>Rescue Team, Mission, Vehicle, Equipment, Shift,<br/>Route, Case Study, Analytics, Export, Monitoring,<br/>Hospital, Shelter, Volunteer, Resource, Drone,<br/>Notification, Audit, Weather, Location"]
        Services["Service Layer<br/>Auth, Disaster, AIPrediction, ResourceOptimization,<br/>RecommendationEngines, RescueTeam, Mission, Shift,<br/>RouteOptimization, Analytics, Export, Notification,<br/>Audit, SystemSetting, LiveTracking, Weather, Email, SMS"]
        Engines["Offline AI Engines<br/>HeuristicPredictionModel, ScoreEngine, ConfidenceEngine,<br/>TimeEstimationEngine, EvacuationEngine,<br/>Hospital/Shelter/Volunteer Recommendation Engines"]
        WebSocket["WebSocket Config + Session Service<br/>(/ws/live broadcaster)"]
        Scheduler["Live Simulation Scheduler<br/>(drone/location simulation)"]
        Repos["Spring Data JPA Repositories<br/>(Specifications + Pageable)"]
    end

    subgraph Data[Data Tier]
        DB[(H2 in-memory dev / PostgreSQL prod)]
        Flyway["Flyway Migrations V1..V7"]
        Files["Static / Uploaded Evidence (DB LOB)"]
    end

    subgraph External[External Providers]
        Mail["Mail Provider (Mock / SMTP)"]
        SMS["SMS Provider (Mock / Twilio)"]
        WeatherAPI["Weather API (Mock / OpenWeatherMap)"]
    end

    Client -->|HTTP/JSON + JWT| Edge
    Edge --> API
    API --> Data
    Browser -->|WebSocket| WebSocket
    Services --> Engines
    Services --> External
    Scheduler --> WebSocket
    Controllers --> Services
    Services --> Repos
    Repos --> DB
    Flyway --> DB

    classDef client fill:#0F4C81,color:#fff;
    classDef api fill:#00897B,color:#fff;
    classDef data fill:#455A64,color:#fff;
    class Client client;
    class API api;
    class Data data;
```

## Layer responsibilities

| Layer | Components | Responsibility |
|-------|------------|----------------|
| **Client** | React 18 + MUI 9, Vite 5 | Rendering, state, routing, map/chat interaction, WebSocket client |
| **Edge** | Vite proxy, security filters | Request routing, rate limiting, CORS, security headers |
| **API** | Spring Boot 3.2 controllers | REST endpoints, DTO mapping, validation, `@PreAuthorize` RBAC |
| **Service** | ~30 services | Business logic, transactions, notifications, audit, AI orchestration |
| **AI engines** | Pure offline components | Deterministic prediction/scoring/recommendation (no external inference) |
| **Data** | JPA/Hibernate + Flyway | Persistence, migrations, indexes, optimistic locking |
| **External** | Mail/SMS/Weather providers | Pluggable; mock implementations are used when credentials are absent |

## Key design decisions

- **Stateless JWT auth** with refresh-token rotation and per-user lockout + per-IP rate limits.
- **Offline-first AI:** the entire prediction/recommendation stack runs deterministically
  on the server, so the platform is fully functional without external ML services.
- **WebSocket push** via raw WebSocket (`/ws/live`) with graceful degradation to polling.
- **Flyway migrations** keep H2 (dev) and PostgreSQL (prod) schemas in sync; Hibernate
  validates entities against the schema at boot.
- **`open-in-view=false`** and explicit `@Transactional(readOnly = true)` on read paths
  avoid lazy-loading surprises and keep connections short.
