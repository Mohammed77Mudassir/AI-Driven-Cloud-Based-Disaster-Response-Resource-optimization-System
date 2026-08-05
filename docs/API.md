# REST API Reference — AI Disaster Management System

Base URL (dev): `http://localhost:8080` — all endpoints are prefixed with `/api` unless noted.

- **Authentication:** most endpoints require `Authorization: Bearer <accessToken>`.
- **Public endpoints:** `/api/auth/*`, `/api/public/*`, `/actuator/health|info`, Swagger.
- **Interactive docs:** Swagger UI at `http://localhost:8080/swagger-ui.html` (OpenAPI
  `/v3/api-docs`).
- **Errors:** JSON body `{"error": "message"}` (validation failures return per-field
  objects). HTTP status codes: `400` bad request, `401` unauthenticated, `403` forbidden,
  `404` not found, `409` conflict (duplicate / optimistic-lock), `413` upload too large,
  `429` rate limited, `500` internal.
- **Rate limits:** authenticated/auth endpoints default to 60 req/min/IP; public endpoints
  20 req/min/IP (configurable).

---

## 1. Authentication — `/api/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Create account `{username, email, password}` |
| POST | `/api/auth/login` | Public | `{username, password}` → access + refresh token |
| GET | `/api/auth/me` | Bearer | Current user profile |
| POST | `/api/auth/refresh` | Public | Rotate refresh token `{refreshToken}` → new pair |
| POST | `/api/auth/logout` | Public | Revoke refresh token `{refreshToken}` |
| POST | `/api/auth/verify-email` | Public | `{token}` verifies email |
| POST | `/api/auth/forgot-password` | Public | `{email}` sends reset link |
| POST | `/api/auth/reset-password` | Public | `{token, newPassword}` resets password |

**Login response** includes: `token` (JWT access), `refreshToken`, `id`, `username`,
`email`, `emailVerified`, `role`, `permissions[]`.

> Security: after `max-attempts` (default 5) failed logins the account is locked for
> `lockout-minutes` (default 15). Refresh tokens are **single-use (rotated)** on every
> refresh and revoked on logout. Deactivated accounts are rejected even with a valid token.

## 2. Users — `/api/users`

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/users` | ADMIN | List users; filters `search`, `role`, `status`, paged |
| GET | `/api/users/{id}` | ADMIN | User detail |
| PUT | `/api/users/{id}` | ADMIN | Update profile fields |
| DELETE | `/api/users/{id}` | ADMIN | Delete user |
| PUT | `/api/users/{id}/activate` | ADMIN | Activate account |
| PUT | `/api/users/{id}/deactivate` | ADMIN | Deactivate account |
| PUT | `/api/users/{id}/role` | ADMIN | Change role `{role}` |
| POST | `/api/users/{id}/reset-password` | ADMIN | Force reset `{newPassword}` |
| POST | `/api/users/change-password` | Bearer | Self-serve change `{currentPassword, newPassword}` |
| GET | `/api/users/profile` | Bearer | Own profile |
| PUT | `/api/users/profile` | Bearer | Update own profile |
| GET | `/api/users/activity/{id}` | ADMIN | Activity history |

## 3. Disasters — `/api/disasters`

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/disasters` | `DISASTER_CREATE` | Report disaster |
| GET | `/api/disasters` | `DISASTER_VIEW` | Paged list; filters `search,type,severity,status,priority,source` |
| GET | `/api/disasters/my` | `DISASTER_VIEW` | Own reports (paged) |
| GET | `/api/disasters/status-flow` | `DISASTER_VIEW` | Allowed status transitions map |
| GET | `/api/disasters/{id}` | `DISASTER_VIEW` | Detail + attachments |
| PUT | `/api/disasters/{id}` | `DISASTER_UPDATE` | Update editable fields |
| PUT | `/api/disasters/{id}/priority` | `DISASTER_UPDATE` | `{priority}` |
| PUT | `/api/disasters/{id}/status` | `DISASTER_UPDATE` | `{status, comment}` (workflow enforced) |
| DELETE | `/api/disasters/{id}` | `DISASTER_DELETE` | Hard delete + dependents |
| GET | `/api/disasters/{id}/detail` | `DISASTER_VIEW` | Full envelope: timeline, comments, assignments, attachments |
| GET | `/api/disasters/{id}/timeline` | `DISASTER_VIEW` | Status timeline |
| GET | `/api/disasters/{id}/comments` | `DISASTER_VIEW` | Comments |
| POST | `/api/disasters/{id}/comments` | `DISASTER_VIEW` | Add comment `{text}` |
| DELETE | `/api/disasters/comments/{commentId}` | owner/ADMIN | Delete comment |
| GET | `/api/disasters/{id}/assignments` | `DISASTER_VIEW` | Team assignment history |

**Status lifecycle:** `PENDING → VERIFIED → ASSIGNED → RESOURCES_DISPATCHED → IN_PROGRESS → RESOLVED`
(transitions validated server-side; `RESOLVED` is terminal).

## 4. AI Engine — `/api/ai`, `/api/predictions`, `/api/recommendations`

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/ai/analyze` | `AI_VIEW` | Full analysis: damage, risk, priority, recovery/response times, confidence, recommendations |
| POST | `/api/ai/recommendations` | `AI_VIEW` | Hospitals, shelters, volunteers, evacuation, resources |
| GET | `/api/ai/models` | `AI_VIEW` | Model metadata (offline) |
| GET | `/api/ai/self-test` | `AI_VIEW` | Runs all engines; returns pass/fail checks |
| POST | `/api/predictions` | `AI_VIEW` | Legacy damage prediction |
| POST | `/api/recommendations` | `AI_VIEW` | Legacy resource allocation |

All AI endpoints are deterministic and fully offline (heuristic models).

## 5. Real-Time Monitoring — `/api/monitoring`, `/api/locations`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/monitoring/overview` | Live overview (counts, alerts, last events) |
| GET | `/api/monitoring/locations` | All tracked locations |
| GET | `/api/monitoring/heatmap` | Heat points for map heat layer |
| GET | `/api/monitoring/distance` | `lat1,lon1,lat2,lon2` → km |
| GET | `/api/monitoring/eta` | `distanceKm, speedKmph` → minutes |
| GET | `/api/monitoring/route` | Polyline length + ETA |
| GET | `/api/monitoring/ws-status` | WebSocket session count |
| GET | `/api/locations` | All location entities |
| GET | `/api/locations/live` | Live location feed |

## 6. Rescue Operations

### Rescue teams — `/api/rescue-teams`
`GET` list (filters `status`, `disasterId`), `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`,
`PUT /{id}/assign/{disasterId}`, `PUT /{id}/status`, `GET /status/{status}`,
`GET /disaster/{disasterId}`, `GET /availability`, `GET /{id}/availability`,
`GET /locations/latest`, `GET /{id}/locations[/latest]`, `POST /{id}/location`.
Members: `GET /{id}/members`, `POST /{id}/members`, `PUT /members/{memberId}`,
`PUT /members/{memberId}/leader`, `DELETE /members/{memberId}`.

### Missions — `/api/missions`
`GET` (filters `status`, `teamId`, `disasterId`), `GET /{id}`, `POST`, `PUT /{id}`,
`PUT /{id}/status`, `DELETE /{id}`, `GET /status/{status}`, `GET /team/{teamId}[/status/{status}]`,
`GET /disaster/{disasterId}`, `GET /transitions/{status}`, `GET /{id}/events`.

### Vehicles — `/api/rescue-vehicles`
`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /status/{status}`,
`GET /team/{teamId}`, `GET /mission/{missionId}`, `PUT /{id}/deploy`, `PUT /{id}/return`,
`PUT /{id}/maintenance[/complete]`.

### Equipment — `/api/rescue-equipment`
`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /status/{status}`,
`GET /team/{teamId}`, `GET /mission/{missionId}`, `PUT /{id}/deploy`, `PUT /{id}/return`,
`PUT /{id}/maintenance[/complete]`.

### Shifts — `/api/shifts`
`GET` (filters `teamId`, `memberId`, date range), `GET /{id}`, `POST`, `PUT /{id}`,
`PUT /{id}/status`, `DELETE /{id}`, `GET /team/{teamId}`, `GET /member/{memberId}`,
`GET /roster?date=`, `GET /roster?from=&to=`.

## 7. Entity Management (Full CRUD)

| Module | Base path | Filters / actions |
|--------|-----------|-------------------|
| Hospitals | `/api/hospitals` | CRUD |
| Shelters | `/api/shelters` | CRUD |
| Volunteers | `/api/volunteers` | CRUD + `GET /available` |
| Resources | `/api/resources` | CRUD + `GET /available`, `GET /disaster/{id}`, `GET /mission/{id}`, deploy/return/maintenance, `GET /movements` (+ filters) |
| Drones | `/api/drones` | CRUD + `PUT /{id}/location` |
| Weather | `/api/weather` | `GET ?location=&latitude=&longitude=` |

## 8. Notifications, Audit, Analytics, Export

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/notifications` | All my notifications |
| GET | `/api/notifications/unread` | Unread only |
| GET | `/api/notifications/unread-count` | Unread badge count |
| PUT | `/api/notifications/{id}/read` | Mark read |
| PUT | `/api/notifications/read-all` | Mark all read |
| GET | `/api/audit-logs` | ADMIN — all audit entries (paged) |
| GET | `/api/audit-logs/user/{username}` | ADMIN — by user |
| GET | `/api/analytics` | 12+ metrics + chart datasets |
| GET | `/api/exports/csv` | CSV download (blob) |
| GET | `/api/exports/pdf` | PDF download (blob) |
| GET | `/api/exports/excel` | Excel-compatible download (blob) |

## 9. Admin — `/api/admin`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/dashboard` | Aggregated operational metrics |
| GET | `/api/admin/notifications` | System notifications |
| GET | `/api/admin/settings` | Key-value settings |
| PUT | `/api/admin/settings/{key}` | Update setting `{value}` |

## 10. Case Studies — `/api/case-studies`

`GET` list, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` (manage = ADMIN),
`GET /search?year=&type=&location=`, `GET /comparison?ids=a,b,c`.

## 11. Route Optimization — `/api/routes`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/routes/calculate` | `{from, to, waypoints, mode}` → polyline, distance, time |

Offline Haversine-based; designed to be swapped for an OSM/routing provider.

## 12. Public Citizen Reports — `/api/public/disasters`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/public/disasters` | Submit report (no auth); returns `reportId` |
| GET | `/api/public/disasters/{reportId}` | Track status by report ID (PII redacted) |
| GET | `/api/public/disasters/{reportId}/timeline` | Public status timeline |

Rate limited more strictly (default 20/min/IP).

## 13. WebSocket

| Endpoint | Protocol | Message types |
|----------|----------|---------------|
| `/ws/live` | Raw WebSocket (no auth) | `DISASTER`, `NOTIFICATION`, `DRONE_LOCATION`, `RESOURCE_LOCATION`, `VOLUNTEER_LOCATION`, `TEAM_LOCATION`, `DASHBOARD` |

Broadcasts are JSON with a `type` field. Reporter PII (mobile/email) is stripped from
public disaster broadcasts. The frontend auto-reconnects and falls back to REST polling.

## 14. Actuator & OpenAPI

- `GET /actuator/health` (public), `GET /actuator/info` (public), `GET /actuator/metrics` (protected).
- `GET /v3/api-docs`, `GET /swagger-ui.html`.
