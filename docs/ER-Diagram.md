# Entity Relationship (ER) Diagram — AI Disaster Management System

> Rendered below as a Mermaid `erDiagram`. GitHub, GitLab and any Mermaid-compatible
> viewer render this natively. The schema is managed with Flyway migrations in
> `backend/src/main/resources/db/migration/V1__init_schema.sql` … `V7__performance_indexes.sql`
> and validated at boot by Hibernate (`ddl-auto=validate`). Works on H2 (PostgreSQL
> compatibility mode) and PostgreSQL.

```mermaid
erDiagram
    users ||--o{ disasters : "reports"
    users ||--o{ notifications : "receives"
    users ||--o{ status_timeline : "changes"
    users ||--o{ verification_tokens : "owns"
    users ||--o{ refresh_tokens : "owns"

    disasters ||--o{ status_timeline : "tracks"
    disasters ||--o{ disaster_comments : "has"
    disasters ||--o{ disaster_assignments : "has"
    disasters ||--o{ disaster_attachments : "has"
    disasters ||--o{ volunteers : "assigns"
    disasters ||--o{ resources : "assigns"
    disasters ||--o{ drones : "assigns"
    disasters ||--o{ rescue_teams : "assigns"
    disasters ||--o{ rescue_missions : "targets"

    rescue_teams ||--o{ team_members : "composes"
    rescue_teams ||--o{ rescue_vehicles : "owns"
    rescue_teams ||--o{ rescue_equipment : "owns"
    rescue_teams ||--o{ rescue_missions : "executes"
    rescue_teams ||--o{ team_location_updates : "tracks"
    rescue_teams ||--o{ team_shifts : "rosters"

    team_members ||--o{ team_shifts : "covers"
    rescue_missions ||--o{ mission_events : "logs"
    rescue_missions ||--o{ resources : "consumes"

    users {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar role
        varchar phone
        varchar address
        boolean active
        boolean email_verified
        timestamp last_login
        int total_reports
        int assigned_disasters
        timestamp created_at
        timestamp updated_at
        bigint version "optimistic lock"
    }
    disasters {
        bigint id PK
        varchar disaster_type
        varchar severity
        varchar status
        varchar priority
        varchar location
        double latitude
        double longitude
        timestamp date
        varchar report_id UK
        varchar source
        varchar reporter_name
        varchar reporter_mobile
        varchar reporter_email
        varchar address
        bigint user_id FK
        bigint version
    }
    hospitals {
        bigint id PK
        varchar name
        int available_beds
        int icu_beds
        int doctors_available
        boolean blood_bank
        double latitude
        double longitude
    }
    shelters {
        bigint id PK
        varchar name
        int capacity
        int occupancy
        boolean food_available
        boolean water_available
        boolean power_available
        double latitude
        double longitude
    }
    volunteers {
        bigint id PK
        varchar name
        varchar skills
        boolean available
        bigint assigned_disaster_id FK
    }
    resources {
        bigint id PK
        varchar resource_type
        int quantity
        int total_quantity
        int deployed_quantity
        int in_maintenance_quantity
        boolean available
        bigint assigned_disaster_id FK
        bigint assigned_mission_id FK
        varchar condition
        timestamp maintenance_due_at
    }
    drones {
        bigint id PK
        varchar drone_id
        varchar status
        int battery
        boolean camera_status
        bigint assigned_disaster_id FK
    }
    notifications {
        bigint id PK
        varchar title
        varchar message
        varchar type
        boolean is_read
        bigint user_id FK
        timestamp created_at
    }
    audit_logs {
        bigint id PK
        varchar action
        varchar entity_type
        bigint entity_id
        varchar performed_by
        varchar details
        timestamp timestamp
    }
    status_timeline {
        bigint id PK
        bigint disaster_id FK
        varchar from_status
        varchar to_status
        timestamp changed_at
        bigint changed_by_id FK
        varchar comment
    }
    disaster_comments {
        bigint id PK
        bigint disaster_id FK
        bigint author_id FK
        varchar text
        timestamp created_at
    }
    disaster_assignments {
        bigint id PK
        bigint disaster_id FK
        bigint team_id FK
        varchar assigned_by
        varchar action
        timestamp assigned_at
        timestamp released_at
    }
    disaster_attachments {
        bigint id PK
        bigint disaster_id FK
        varchar category
        varchar filename
        varchar mime_type
        bigint size
        clob data_url
        timestamp created_at
    }
    rescue_teams {
        bigint id PK
        varchar team_name
        varchar status
        varchar specialty
        int max_capacity
        bigint assigned_disaster_id FK
        bigint version
    }
    team_members {
        bigint id PK
        varchar name
        varchar role
        varchar speciality
        boolean is_leader
        boolean available
        bigint team_id FK
    }
    rescue_vehicles {
        bigint id PK
        bigint team_id FK
        varchar vehicle_type
        varchar registration_number
        varchar status
        int fuel_level
        bigint version
    }
    rescue_equipment {
        bigint id PK
        bigint team_id FK
        varchar equipment_type
        int total_quantity
        int available_quantity
        int deployed_quantity
        varchar status
        bigint version
    }
    rescue_missions {
        bigint id PK
        varchar mission_code
        varchar mission_type
        varchar status
        varchar priority
        bigint team_id FK
        bigint disaster_id FK
        timestamp start_time
        timestamp end_time
        bigint version
    }
    mission_events {
        bigint id PK
        bigint mission_id FK
        varchar event_type
        varchar message
        timestamp occurred_at
    }
    team_location_updates {
        bigint id PK
        bigint team_id FK
        double latitude
        double longitude
        double speed
        timestamp timestamp
    }
    team_shifts {
        bigint id PK
        bigint team_id FK
        bigint member_id FK
        varchar shift_type
        varchar shift_status
        timestamp shift_start
        timestamp shift_end
        bigint version
    }
    verification_tokens {
        bigint id PK
        varchar token UK
        bigint user_id FK
        varchar type
        timestamp expiry_time
        boolean used
    }
    refresh_tokens {
        bigint id PK
        varchar token UK
        bigint user_id FK
        timestamp expiry_time
        boolean revoked
    }
    system_settings {
        bigint id PK
        varchar setting_key UK
        varchar setting_value
    }
    case_studies {
        bigint id PK
        varchar title
        varchar disaster_type
        varchar location
        int disaster_year
        varchar severity
        double estimated_damage
        int affected_population
        double response_time_hours
        double recovery_time_days
    }
```

## Key relationships

| From | To | Cardinality | Meaning |
|------|-----|-------------|---------|
| `users` | `disasters` | 1 : N | A user reports many disasters |
| `users` | `notifications` | 1 : N | Each notification targets one user |
| `disasters` | `status_timeline` | 1 : N | Full status history per disaster |
| `disasters` | `disaster_comments` | 1 : N | Collaboration thread per disaster |
| `disasters` | `disaster_attachments` | 1 : N | Photo/PDF/video evidence |
| `disasters` | `rescue_teams` | 1 : N | Teams assigned to a disaster |
| `rescue_teams` | `team_members` | 1 : N | Team composition |
| `rescue_teams` | `rescue_vehicles` / `rescue_equipment` | 1 : N | Assets owned by a team |
| `rescue_teams` | `rescue_missions` | 1 : N | Missions executed by a team |
| `rescue_missions` | `mission_events` | 1 : N | Mission event log |
| `rescue_teams` | `team_shifts` | 1 : N | Duty roster; each shift also references a `team_members` row |
| `users` | `verification_tokens` / `refresh_tokens` | 1 : N | Email verification and session refresh |

## Concurrency & integrity notes

- **Optimistic locking:** `version` columns on `users`, `disasters`, `rescue_teams`,
  `team_members`, `rescue_vehicles`, `rescue_equipment`, `rescue_missions`, `team_shifts`
  and `resources` are incremented on write. Conflicts surface as
  `409 Conflict` via `GlobalExceptionHandler`.
- **Soft state:** user accounts use an `active` flag; no rows are physically removed.
- **Indexes:** composite indexes exist for the hottest paths
  (`notifications(user_id, is_read)`, `team_location_updates(team_id, timestamp)`,
  `team_shifts(shift_start, shift_end)`, `team_members(team_id, is_leader)`) and are
  defined in migration `V7__performance_indexes.sql`.
