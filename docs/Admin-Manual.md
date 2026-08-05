# Admin Manual — AI Disaster Management System

This guide covers administrator duties: users, disaster workflow, rescue operations,
AI, exports, settings and health monitoring. Regular users should see the
[User Manual](User-Manual.md).

---

## 1. Administrator sign-in

- Use `admin` / `admin123` (demo) or the **Demo Admin** button on the login page.
- The Admin role has every permission; the **Admin Dashboard** and
  **Admin Control Center** are available from the sidebar.

---

## 2. Disaster command & control

### Status lifecycle

```
PENDING → VERIFIED → ASSIGNED → RESOURCES_DISPATCHED → IN_PROGRESS → RESOLVED
```

- **Verify** incoming reports (including public citizen reports).
- **Assign** rescue teams and resources to the incident.
- Drive the disaster to **RESOLVED**; terminal state — further transitions are rejected.
- Use **Status Update** with an optional comment; the reporter is notified by
  in-app notification, SMS and email, and the timeline records the change.

### Disaster list & filters
The disaster list supports pagination and filters for search text, type, severity,
status, priority and source. Sort by date, type, severity, status, priority, location.

---

## 3. User management

**Users** screen (ADMIN):

| Action | Where |
|--------|-------|
| List / search / filter | Search box, role & status filters |
| Edit user | Pencil icon → update profile fields |
| Change role | Role dropdown in edit |
| Activate / deactivate | Toggle; deactivated users are immediately blocked even with a valid token |
| Reset password | "Reset password" action → forces new password on next login |
| Delete user | Trash icon (audit-logged) |
| View activity | Activity tab shows login/report/edit history |

> Best practice: deactivate rather than delete to preserve the audit trail and
> foreign-key integrity.

---

## 4. Rescue operations

### Teams
- Create teams with name, specialty, capacity, leader and contact.
- **Assign** teams to a disaster; the assignment is recorded in `disaster_assignments`.
- Manage members (add/remove, set leader), vehicles and equipment per team.
- Track team location live (`/locations/latest`) and availability.

### Missions
- Create missions tied to a team and disaster with priority and instructions.
- Advance through mission statuses (validation enforced via `/transitions`).
- Each state change writes a `mission_event` for the audit trail.

### Shifts / duty roster
- Build rosters by team/member and query by date or range.
- Track shift types (morning/evening/night) and statuses.

### Vehicles & equipment
- Full inventory with total/available/deployed/maintenance quantities.
- **Deploy** stock to a mission, **return** it, and run **maintenance** cycles.
- Movement history is captured for every inventory transaction.

---

## 5. AI engine (offline)

- `GET /api/ai/self-test` runs every engine and returns a pass/fail report — use it
  as a post-deployment smoke test.
- `GET /api/ai/models` returns model metadata.
- Analysis and recommendation endpoints are **deterministic** and offline; the same
  input always yields the same output, which makes results auditable.

---

## 6. Exports

- **CSV / PDF / Excel** exports are available from Analytics / export screens and
  via `/api/exports/*`. They include the current filtered dataset.
- Downloads are delivered as `Content-Disposition` attachments.

---

## 7. Admin Control Center

| Section | Contents |
|---------|----------|
| Dashboard | Aggregated operational KPIs |
| Settings | Key-value system settings (e.g. system name, version, maintenance mode) |
| Notifications | System-wide notification feed |

---

## 8. Audit trail

Every meaningful action writes an `audit_logs` entry:

| Field | Example |
|-------|---------|
| `action` | `CREATE`, `STATUS_CHANGE`, `LOGIN`, `PUBLIC_REPORT`, `DELETE` |
| `entityType` | `Disaster`, `User`, `RescueTeam`, … |
| `performedBy` | username |
| `details` | human-readable summary |

View the audit log in the **Audit Logs** screen; it is ADMIN-only.

---

## 9. Configuration reference

Key properties (see `backend/src/main/resources/application.properties`):

| Property | Default | Meaning |
|----------|---------|---------|
| `app.jwt.secret` | demo secret | **Must be overridden via `JWT_SECRET` in production** |
| `app.jwt.expiration-ms` | 86400000 | Access token lifetime |
| `app.jwt.refresh-expiration-ms` | 604800000 | Refresh token lifetime |
| `app.security.login.max-attempts` | 5 | Lockout threshold |
| `app.security.login.lockout-minutes` | 15 | Lockout duration |
| `app.security.rate-limit.max-per-minute` | 60 | Authenticated endpoint limit/IP |
| `app.security.rate-limit.public-max-per-minute` | 20 | Public endpoint limit/IP |
| `app.security.h2-console-enabled` | false | H2 console (dev profile only) |
| `spring.servlet.multipart.max-file-size` | 5MB | Attachment upload limit |
| `app.cors.allowed-origins` | http://localhost:5173 | CORS allow-list |

### Profiles
- **default** — H2 in-memory, console **disabled**.
- **dev** — `--spring.profiles.active=dev` enables the H2 console and debug logs.
- **postgres** — `application-postgres.properties` for PostgreSQL; Flyway runs the
  same migrations automatically.

---

## 10. Running & validating a release

```bash
# Backend
cd backend
mvn clean verify            # compiles + runs 112 tests (integration + unit)
java -jar target/ai-disaster-management-2.0.0.jar --spring.profiles.active=postgres

# Frontend
cd frontend
npm ci
npm run build               # production bundle in dist/
```

Smoke checks after deploy:
1. `GET /actuator/health` → `UP`.
2. Login as admin → Admin Dashboard loads.
3. `GET /api/ai/self-test` → all checks pass.
4. Report a test disaster → notification + WebSocket feed update appears.
5. Create/update/delete one of each managed entity (hospital, shelter, volunteer,
   resource, drone, team, mission).
6. Export CSV/PDF and confirm downloads.

---

## 11. Backup & disaster recovery

- Back up PostgreSQL: `pg_dump -Fc disaster_db > disaster_db.dump` (schedule + retention).
- Attachments are stored in the database, so a full dump captures everything.
- Test restore on a staging instance before relying on it.
