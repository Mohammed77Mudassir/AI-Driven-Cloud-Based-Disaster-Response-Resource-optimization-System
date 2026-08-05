# Use Case Diagram — AI Disaster Management System

> Rendered as a Mermaid `flowchart`. Actors are drawn from the RBAC matrix in
> `RbacService` plus the unauthenticated public citizen.

```mermaid
flowchart LR
    subgraph System[AI Disaster Management Platform]
        direction TB
        AUTH[Authenticate / Register / Forgot Password]
        DIS[Report & Manage Disasters]
        TRACK[Track Public Report]
        AI[AIAnalysis: Damage / Risk / Priority / Recovery]
        RECO[Recommendations: Hospital / Shelter / Volunteer / Evacuation / Resources]
        RES[Manage Resources, Drones, Hospitals, Shelters, Volunteers]
        TEAM[Rescue Team Coordination]
        MISSION[Mission Planning & Execution]
        SHIFT[Duty Roster / Shifts]
        ROUTE[Route Optimization]
        MON[Real-Time Monitoring & WebSocket Feed]
        ANALYTICS[Analytics & Exports]
        NOTIFY[Notifications / SMS / Email]
        AUDIT[Audit Trail]
        USERS[User Management]
        SETTINGS[System Settings & Health]
        CASE[Case Study Analysis]
    end

    Public[Public Citizen] -->|reports incident| DIS
    Public -->|tracks by report ID| TRACK
    Public -->|verify email / reset password| AUTH

    User[Registered User / Citizen] --> AUTH
    User --> DIS
    User --> AI
    User --> RECO
    User --> MON
    User --> NOTIFY
    User --> CASE
    User --> TRACK

    DMO[Disaster Management Officer] --> User
    DMO --> RES
    DMO --> TEAM
    DMO --> MISSION
    DMO --> SHIFT
    DMO --> ROUTE
    DMO --> ANALYTICS

    Police[Police] --> AUTH
    Police --> DIS
    Police --> AI
    Police --> TEAM
    Police --> ROUTE

    FireDept[Fire Department] --> AUTH
    FireDept --> DIS
    FireDept --> AI
    FireDept --> TEAM
    FireDept --> ROUTE

    HospitalStaff[Hospital Staff] --> AUTH
    HospitalStaff --> RES
    HospitalStaff --> TEAM
    HospitalStaff --> AI

    RescueTeam[Rescue Team] --> AUTH
    RescueTeam --> MISSION
    RescueTeam --> ROUTE
    RescueTeam --> MON
    RescueTeam --> DIS

    VolunteerCoord[Volunteer Coordinator] --> AUTH
    VolunteerCoord --> RES
    VolunteerCoord --> DIS

    NGOCoord[NGO Coordinator] --> AUTH
    NGOCoord --> RES
    NGOCoord --> DIS

    Admin[System Administrator] --> AUTH
    Admin --> USERS
    Admin --> SETTINGS
    Admin --> AUDIT
    Admin --> ANALYTICS
    Admin --> DIS
    Admin --> TEAM
    Admin --> RES
```

## Actor / permission summary

| Actor | Key capabilities | RBAC role |
|-------|------------------|-----------|
| Public Citizen | Submit and track disaster reports (no login) | — (public endpoints) |
| Registered User | Report disasters, run AI analysis, view maps/analytics, notifications | `USER` |
| Disaster Management Officer | Command & control: disasters, rescue teams, missions, shifts, resources, analytics | `DMO` |
| Police | View/report disasters, view teams, routing | `POLICE` |
| Fire Department | View/report disasters, view teams, routing | `FIRE_DEPARTMENT` |
| Hospital Staff | Manage hospitals, view disasters/resources/teams | `HOSPITAL_STAFF` |
| Rescue Team | View assigned missions, routing, live monitoring | `RESCUE_TEAM` |
| Volunteer Coordinator | Manage volunteers and shelters | `VOLUNTEER_COORDINATOR` |
| NGO Coordinator | Manage shelters, view volunteers | `NGO_COORDINATOR` |
| System Administrator | Full access: users, settings, health, audit, everything | `ADMIN` |

The permission matrix is enforced server-side via
`@PreAuthorize("@rbacService.hasPermission(...)")` and mirrored to the frontend in the
login payload for menu/route filtering.
