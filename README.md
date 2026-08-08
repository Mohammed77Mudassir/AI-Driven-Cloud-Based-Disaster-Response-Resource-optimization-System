# AI Disaster Management System v3.0 - Enterprise Platform

A professional enterprise-grade Disaster Response Platform with AI-powered features, real-time WebSocket monitoring, comprehensive resource management, and Material UI command center interface. Designed for Government Emergency Operations Centers (EOCs), disaster management authorities, and humanitarian organizations.

---

## Version 3.0 - Enterprise Command Center

### New Features (v3.0)

| Module | Description | Status |
|--------|-------------|--------|
| **User Management** | Complete user CRUD, search/filter, role management, activate/deactivate, password reset, activity tracking | ✅ |
| **Advanced Login** | Remember Me,  professional auth UI | ✅ |
| **Real-Time WebSocket** | Live push for disasters, notifications, drones, locations via WebSocket | ✅ |
| **Enhanced AI Engine** | Priority score, risk score, confidence %, response/recovery time estimates | ✅ |
| **Rescue Team Coordination** | Team CRUD, member management, assignment to disasters, status tracking, scheduling | ✅ |
| **Route Optimization** | Distance/time calculation, multi-destination routing, Haversine formula | ✅ |
| **Case Study Analysis** | Historical disaster analysis, lessons learned, comparison charts, search by year/type/location | ✅ |
| **Admin Control Center** | Unified admin panel with settings, quick actions, dashboard metrics | ✅ |
| **Material UI Redesign** | Professional EOC theme: Deep Blue (#0F4C81), Teal, Orange accent | ✅ |
| **Dark Mode Ready** | Theme infrastructure with dark/light toggle support | ✅ |
| **Loading Skeletons** | Professional loading states for tables, stats, charts | ✅ |
| **Breadcrumb Navigation** | Dynamic breadcrumbs on every page | ✅ |
| **Animated Counters** | Smooth number animations on dashboard stats | ✅ |
| **Empty States** | Professional empty state screens with actions | ✅ |
| **WebSocket Status** | Live connection indicator with green/red chip | ✅ |
| **Footer** | Professional footer with version info | ✅ |
| **404/403 Pages** | Professional error pages | ✅ |
| **User Profiles** | Editable profile with activity history and security settings | ✅ |

### Preserved Features (v1.0 - v2.0)

- JWT-based User Authentication and Registration
- Role-based Access Control (USER / ADMIN)
- Disaster Reporting with 8 types, 4 severities, 6-status lifecycle
- AI Damage Prediction and Resource Optimization
- Interactive Maps with Street, Terrain, Satellite layers
- Live Drone GPS Simulation with auto-refresh
- Full CRUD: Hospitals, Shelters, Volunteers, Resources, Drones
- In-app Notification Center with unread badge
- SMS and Email alert mocks (Twilio/Spring Mail-ready)
- Advanced Analytics: 12+ metrics, 4 chart types, utilization rates
- Export Reports: CSV, TXT/PDF
- Audit Logging with complete activity trail
- Status Timeline per disaster
- H2 in-memory database with comprehensive seed data (dev); PostgreSQL profile for production

---

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT (jjwt 0.12.3, BCrypt)
- Spring Data JPA + Hibernate
- Spring WebSocket (STOMP-compatible)
- Spring Validation
- H2 Database (in-memory, dev) / PostgreSQL (production, Flyway-managed)
- OpenCSV 5.9 for exports
- Maven

### Frontend
- React 18.2.0
- Vite 5
- Material UI (MUI) 9.x + Icons
- React Router 6
- Axios 1.6
- Recharts 2.10 (analytics)
- React Leaflet 4.2 + OpenStreetMap
- STOMP.js + SockJS (WebSocket)
- React Hot Toast 2.4
- Emotion (MUI styling engine)

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     Frontend (:5173)                              │
│   MUI + React + Vite + Recharts + Leaflet + WebSocket Client     │
└──────────────┬───────────────────────────────────────────────────┘
               │  HTTP/JSON + JWT (via Vite proxy)    │ WebSocket
┌──────────────▼───────────────────────────────────────────────────┐
│                     Backend (:8080)                               │
│   Spring Boot 3.2 + Spring Security + Spring Data JPA            │
│                                                                  │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│   │ Auth     │ │ Disaster │ │ AI       │ │ WebSocket        │   │
│   │ Module   │ │ Module   │ │ Module   │ │ Broadcaster      │   │
│   ├──────────┤ ├──────────┤ ├──────────┤ ├──────────────────┤   │
│   │ User Mgmt│ │ Rescue   │ │ Route    │ │ Case Study       │   │
│   │ Module   │ │ Teams    │ │ Planner  │ │ Module           │   │
│   ├──────────┤ ├──────────┤ ├──────────┤ ├──────────────────┤   │
│   │ Hospital │ │ Shelter  │ │Volunteer │ │ Drone / Resource │   │
│   │ Module   │ │ Module   │ │ Module   │ │ Module           │   │
│   ├──────────┤ ├──────────┤ ├──────────┤ ├──────────────────┤   │
│   │ Weather  │ │ Map/Loc  │ │ Notify   │ │ Analytics/Export │   │
│   │ Service  │ │ Service  │ │ Service  │ │ Service          │   │
│   └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
└──────────────┬───────────────────────────────────────────────────┘
               │  JPA/Hibernate
┌──────────────▼───────────────────────────────────────────────────┐
│              H2 Database (In-Memory)                              │
│   Tables: users, disasters, hospitals, shelters, volunteers,      │
│   resources, drones, notifications, audit_logs, status_timeline,  │
│   rescue_teams, team_members, case_studies, system_settings      │
└──────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
AI-Disaster-Management-System/
├── backend/
│   ├── src/main/java/com/disaster/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── DataSeeder.java
│   │   │   └── WebSocketConfig.java
│   │   ├── controller/          (21 controllers)
│   │   │   ├── AuthController.java
│   │   │   ├── DisasterController.java
│   │   │   ├── UserController.java          ★ NEW
│   │   │   ├── RescueTeamController.java    ★ NEW
│   │   │   ├── RouteController.java         ★ NEW
│   │   │   ├── CaseStudyController.java     ★ NEW
│   │   │   ├── AdminController.java         ★ NEW
│   │   │   └── + 14 existing controllers
│   │   ├── dto/                 (26 DTOs)
│   │   │   ├── UserDTO, UpdateUserRequest   ★ NEW
│   │   │   ├── RescueTeamDTO, TeamMemberDTO ★ NEW
│   │   │   ├── RouteRequest, RouteResponse  ★ NEW
│   │   │   ├── CaseStudyDTO, CaseStudyReq   ★ NEW
│   │   │   ├── AdminDashboardDTO            ★ NEW
│   │   │   ├── EnhancedPredictionDTO        ★ NEW
│   │   │   ├── LiveUpdateDTO                ★ NEW
│   │   │   └── + 14 existing DTOs
│   │   ├── entity/             (14 entities)
│   │   │   ├── User (enhanced)              ★ ENHANCED
│   │   │   ├── RescueTeam                   ★ NEW
│   │   │   ├── TeamMember                   ★ NEW
│   │   │   ├── CaseStudy                    ★ NEW
│   │   │   ├── SystemSetting                ★ NEW
│   │   │   └── + 9 existing entities
│   │   ├── enums/              (4 enums)
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler, ResourceNotFoundException
│   │   ├── repository/         (14 repos)
│   │   │   ├── RescueTeamRepository         ★ NEW
│   │   │   ├── TeamMemberRepository         ★ NEW
│   │   │   ├── CaseStudyRepository          ★ NEW
│   │   │   ├── SystemSettingRepository      ★ NEW
│   │   │   └── + 10 existing repos
│   │   ├── security/
│   │   │   ├── JwtUtils, JwtAuthFilter, UserDetailsImpl/Service
│   │   ├── service/            (25+ services)
│   │   │   ├── UserService / UserServiceImpl          ★ NEW
│   │   │   ├── RescueTeamService / Impl               ★ NEW
│   │   │   ├── RouteOptimizationService               ★ NEW
│   │   │   ├── CaseStudyService / Impl                ★ NEW
│   │   │   ├── SystemSettingService / Impl            ★ NEW
│   │   │   ├── WebSocketSessionService                ★ NEW
│   │   │   ├── AIPredictionService (enhanced)         ★ ENHANCED
│   │   │   ├── ResourceOptimizationService (enhanced) ★ ENHANCED
│   │   │   └── + 15 existing services
│   │   └── DisasterManagementApplication.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Sidebar.jsx (MUI)
│   │   │   ├── ProtectedRoute.jsx (MUI)
│   │   │   ├── MapView.jsx
│   │   │   ├── NotificationBell.jsx
│   │   │   ├── Breadcrumbs.jsx              ★ NEW
│   │   │   ├── Footer.jsx                   ★ NEW
│   │   │   ├── WebSocketStatus.jsx          ★ NEW
│   │   │   ├── AnimatedCounter.jsx          ★ NEW
│   │   │   ├── LoadingSkeleton.jsx          ★ NEW
│   │   │   ├── EmptyState.jsx               ★ NEW
│   │   │   └── ErrorBoundary.jsx            ★ NEW
│   │   ├── context/
│   │   │   ├── AuthContext.jsx (enhanced)
│   │   │   └── WebSocketContext.jsx         ★ NEW
│   │   ├── pages/               (22 pages)
│   │   │   ├── Login.jsx (MUI redesign)
│   │   │   ├── Register.jsx
│   │   │   ├── UserDashboard.jsx (MUI)
│   │   │   ├── AdminDashboard.jsx (MUI)
│   │   │   ├── UserManagement.jsx           ★ NEW
│   │   │   ├── RescueTeamManagement.jsx     ★ NEW
│   │   │   ├── RouteOptimization.jsx        ★ NEW
│   │   │   ├── CaseStudyAnalysis.jsx        ★ NEW
│   │   │   ├── AdminControlCenter.jsx       ★ NEW
│   │   │   ├── UserProfile.jsx             ★ NEW
│   │   │   ├── + 12 existing pages (all MUI-enhanced)
│   │   ├── services/
│   │   │   └── api.js (extended)
│   │   ├── theme.js                         ★ NEW
│   │   ├── App.jsx, App.css, main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── README.md
```

---

## Installation and Setup

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- Maven 3.8+

### Backend Setup

```bash
cd AI-Disaster-Management-System/backend
mvn clean install
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`.

> The H2 web console is **disabled by default** for production safety. To enable it in
> local development, start with the dev profile:
> `mvn spring-boot:run -Dspring-boot.run.profiles=dev`, then open
> `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:disasterdb`).

### Frontend Setup

```bash
cd AI-Disaster-Management-System/frontend
npm install
npm run dev
```

Frontend starts on `http://localhost:5173`.

### Run Both

Start backend first, then frontend. The frontend proxies API requests to the backend via Vite.

---

---

## Complete Feature Matrix

### 🔐 Authentication & User Management
| Feature | Access |
|---------|--------|
| Self-registration | Public |
| Username/password login | Public |
| JWT authentication | All |
| Remember Me | All |
| Demo User / Admin login buttons | Public |
| View all users | ADMIN |
| Search/filter users | ADMIN |
| Edit user profiles | ADMIN |
| Change user roles | ADMIN |
| Activate/deactivate accounts | ADMIN |
| Reset passwords | ADMIN |
| Delete users | ADMIN |
| View user activity | ADMIN |
| User profile (self) | USER |
| Change own password | USER |

### 🌪️ Disaster Management
| Feature | Role |
|---------|------|
| Report disaster (8 types, 4 severities) | USER |
| View my disasters | USER |
| View all disasters | ADMIN |
| Update disaster status (6-status lifecycle) | ADMIN |
| Delete disaster | ADMIN |
| Search/filter disasters | ALL |
| Status timeline per disaster | ALL |
| Audit trail for all actions | ADMIN |

### 🤖 AI Engine
| Feature | Role |
|---------|------|
| Damage prediction (type × severity) | ALL |
| Affected population estimate | ALL |
| Economic loss estimate (₹) | ALL |
| Recovery time estimate | ALL |
| **Priority score (1-100)** | ALL |
| **Risk score (1-100)** | ALL |
| **Confidence percentage** | ALL |
| **Response time estimate** | ALL |
| Resource recommendations (9 types) | ALL |
| Natural-language reasoning | ALL |

### 🚁 Real-Time Monitoring
| Feature | Description |
|---------|-------------|
| WebSocket live updates | Real-time push for disasters, notifications, drone locations |
| Live connection indicator | Green/red chip in dashboard |
| Drone GPS simulation | 5s auto-refresh |
| Map with 3 layer types | Street, Terrain, Satellite |
| Color-coded markers | Red/Critical, Orange/High, Yellow/Med, Green/Low, Blue |

### 🚑 Rescue Team Coordination
| Feature | Role |
|---------|------|
| Create rescue teams | ADMIN |
| Assign teams to disasters | ADMIN |
| Track team status (6 statuses) | ADMIN |
| Manage team members | ADMIN |
| Team scheduling | ADMIN |
| View team history | ADMIN |
| Equipment and vehicle tracking | ADMIN |

### 🗺️ Route Optimization
| Feature | Description |
|---------|-------------|
| Point-to-point routing | Distance + time calculation |
| Multi-destination routing | Waypoint support |
| Fastest / Shortest modes | Toggle |
| Map visualization | Leaflet with route polyline |
| Haversine formula | No external API dependency |

### 📋 Case Study Analysis
| Feature | Role |
|---------|------|
| Historical disaster records | ALL |
| Lessons learned tracking | ALL |
| Damage/resource comparison | ALL |
| Search by year, type, location | ALL |
| Comparison charts (response time, damage) | ALL |
| Create/edit/delete case studies | ADMIN |

### 🏥 Entity Management (Full CRUD)
| Entity | Features |
|--------|----------|
| Hospitals | Beds, ICU, doctors, blood bank, map |
| Shelters | Capacity, occupancy %, food/water/power, map |
| Volunteers | Skills, availability, deployment |
| Resources | 8 types, inventory grid, assignment |
| Drones | Battery %, camera, mission status, map |

### 📊 Analytics Dashboard
| Metric | Type |
|--------|------|
| Monthly disaster trends | Line chart |
| Disasters by type | Bar chart |
| Disasters by severity | Bar chart |
| Disasters by status | Pie chart |
| Average response time | Numeric |
| Average resolution time | Numeric |
| Resource utilization % | Progress bar |
| Volunteer activity % | Progress bar |
| Hospital occupancy % | Progress bar |
| Shelter occupancy % | Progress bar |

### 🔔 Notifications
| Feature | Description |
|---------|-------------|
| In-app notification center | All notifications |
| Unread count badge | Sidebar bell |
| Mark single/all as read | Actions |
| Type-based styling | Info/Warning/Alert/Success |
| SMS alerts (mock) | On disaster create/status change |
| Email alerts (mock) | On disaster create/status change |

### ⚙️ Admin Control Center
| Feature | Description |
|---------|-------------|
| System settings | Key-value editable |
| Quick actions | Export, audit, settings |
| Aggregated dashboard | All metrics in one view |

---

## Seed Data

On first startup, the application automatically populates:

| Entity | Count | Details |
|--------|-------|---------|
| Users | 2 | admin (ADMIN), user (USER) |
| Disasters | 4 | Flood (Mumbai), Earthquake (Guwahati), Cyclone (Chennai), Wildfire (Dehradun) |
| Hospitals | 3 | Mumbai, Guwahati, Chennai |
| Shelters | 3 | Mumbai, Guwahati, Chennai |
| Volunteers | 3 | Various skills |
| Resources | 5 | Ambulances, Fire Trucks, Medical Teams, Boats, Helicopter |
| Drones | 3 | DRN-001 (Available), DRN-002 (In Mission), DRN-003 (Charging) |
| Rescue Teams | 3 | Alpha, Bravo, Charlie squads |
| Team Members | 3 | Leaders and medics assigned to teams |
| Case Studies | 2 | Mumbai Floods 2023, Guwahati Earthquake 2022 |
| System Settings | 3 | System name, version, maintenance mode |

---

## API Documentation

### Authentication (Public)
| Method | Endpoint |
|--------|----------|
| POST | `/api/auth/register` |
| POST | `/api/auth/login` |

### Users (ADMIN for management, USER for own profile)
| Method | Endpoint |
|--------|----------|
| GET | `/api/users` |
| GET | `/api/users/{id}` |
| PUT | `/api/users/{id}` |
| DELETE | `/api/users/{id}` |
| PUT | `/api/users/{id}/activate` |
| PUT | `/api/users/{id}/deactivate` |
| PUT | `/api/users/{id}/role` |
| POST | `/api/users/{id}/reset-password` |
| POST | `/api/users/change-password` |
| GET | `/api/users/profile` |
| PUT | `/api/users/profile` |
| GET | `/api/users/activity/{id}` |

### Disasters
| Method | Endpoint | Role |
|--------|----------|------|
| POST | `/api/disasters` | USER |
| GET | `/api/disasters/my` | USER |
| GET | `/api/disasters` | ADMIN |
| PUT | `/api/disasters/{id}/status` | ADMIN |
| DELETE | `/api/disasters/{id}` | ADMIN |

### Rescue Teams (ADMIN for mutations)
| Method | Endpoint |
|--------|----------|
| GET | `/api/rescue-teams` |
| GET | `/api/rescue-teams/{id}` |
| POST | `/api/rescue-teams` |
| PUT | `/api/rescue-teams/{id}` |
| DELETE | `/api/rescue-teams/{id}` |
| PUT | `/api/rescue-teams/{id}/assign/{disasterId}` |
| PUT | `/api/rescue-teams/{id}/status` |
| GET | `/api/rescue-teams/status/{status}` |
| GET | `/api/rescue-teams/disaster/{disasterId}` |
| GET | `/api/rescue-teams/{id}/members` |
| POST | `/api/rescue-teams/{id}/members` |
| DELETE | `/api/rescue-teams/members/{memberId}` |

### Route Optimization
| Method | Endpoint |
|--------|----------|
| POST | `/api/routes/calculate` |

### Case Studies
| Method | Endpoint | Role |
|--------|----------|------|
| GET | `/api/case-studies` | ALL |
| GET | `/api/case-studies/{id}` | ALL |
| GET | `/api/case-studies/search` | ALL |
| GET | `/api/case-studies/comparison` | ALL |
| POST | `/api/case-studies` | ADMIN |
| PUT | `/api/case-studies/{id}` | ADMIN |
| DELETE | `/api/case-studies/{id}` | ADMIN |

### AI & Predictions
| Method | Endpoint |
|--------|----------|
| POST | `/api/predictions` |
| POST | `/api/recommendations` |

### Admin Control
| Method | Endpoint |
|--------|----------|
| GET | `/api/admin/dashboard` |
| GET | `/api/admin/settings` |
| PUT | `/api/admin/settings/{key}` |

### WebSocket
| Endpoint | Protocol |
|----------|----------|
| `/ws/live` | Raw WebSocket |

Messages broadcast types: `DISASTER`, `NOTIFICATION`, `DRONE_LOCATION`, `RESOURCE_LOCATION`, `VOLUNTEER_LOCATION`, `DASHBOARD`

### Existing Endpoints (Preserved)
- CRUD: Hospitals, Shelters, Volunteers, Resources, Drones
- Notifications, Audit Logs, Analytics, Exports
- Locations, Timelines, Weather

---

## Design Decisions

### Material UI Theme
- **Primary:** Deep Blue (#0F4C81) — Trust, authority, professionalism
- **Secondary:** Teal (#00897B) — Calm, stability
- **Accent:** Orange (#F57C00) — Urgency, warnings
- **Success:** Green (#2E7D32) — Safe, resolved
- **Background:** Light Gray (#F4F6F8) — Clean, fatigue-reducing

### AI Module
- `PredictionEngine` interface allows ML model integration (TensorFlow/ONNX)
- Enhanced with priority score, risk score, confidence percentage
- Resource optimization includes response/recovery time estimation

### WebSocket Architecture
- Raw WebSocket at `/ws/live` for maximum compatibility
- `WebSocketConfig` broadcaster pattern for server-side push
- `WebSocketContext` with auto-reconnect and listener pattern on frontend
- Graceful fallback: UI continues working if WebSocket disconnects

### Route Optimization
- Haversine formula for accurate distance calculation
- No external API dependency — works offline
- Designed for OSM routing API integration (swap `RouteOptimizationService` implementation)
- Supports multi-destination waypoint routing

---

## Documentation

The full technical and operational documentation lives in [`docs/`](docs/):

| Document | Contents |
|----------|----------|
| [API.md](docs/API.md) | Complete REST API reference (14 modules, WebSocket, error model, rate limits) |
| [ER-Diagram.md](docs/ER-Diagram.md) | Entity-relationship diagram (Mermaid) + schema notes |
| [Use-Case-Diagram.md](docs/Use-Case-Diagram.md) | Actors and use cases (Mermaid) + RBAC matrix |
| [Architecture-Diagram.md](docs/Architecture-Diagram.md) | 3-tier architecture (Mermaid) + design decisions |
| [Deployment-Diagram.md](docs/Deployment-Diagram.md) | Dev & production topologies, Docker Compose, runbook |
| [PRODUCTION-HARDENING.md](docs/PRODUCTION-HARDENING.md) | Security posture, PostgreSQL env config, Flyway, runbook |
| [User-Manual.md](docs/User-Manual.md) | End-user guide |
| [Admin-Manual.md](docs/Admin-Manual.md) | Administrator & operations guide |

Interactive API docs are also served at `http://localhost:8080/swagger-ui.html`.

---

## Testing

```bash
cd backend
mvn test        # 112 tests: integration suites + unit tests
```

Coverage highlights:
- **Integration tests** (61): auth, AI engine, disasters, EOC, WebSocket/monitoring,
  rescue-team coordination, admin dashboard.
- **Unit tests** (51): JWT utilities, rate limiting, account lockout, RBAC matrix,
  geospatial math (Haversine), AI scoring engine.


## License

This project is created for educational and demonstration purposes.
