# User Manual — AI Disaster Management System

This guide covers the platform from the perspective of a **regular registered user**
and a **public citizen**. Administrators should also read the
[Admin Manual](Admin-Manual.md).

---

## 1. Getting started

1. Open the application URL (dev: `http://localhost:5173`).
2. Sign in with your account or use **Register** to create one.
3. Use the **Demo User** button on the login page for instant access
   (`user` / `user123`).

### Signing in

| Field | Notes |
|-------|-------|
| Username | Your login name |
| Password | Your password |
| Remember me | Keeps you signed in across browser restarts (local storage) |

- After **5 failed attempts** your account is temporarily locked for **15 minutes**.
- Forgot your password? Use **Forgot password** → enter your email → click the reset
  link you receive → set a new password.

---

## 2. Navigation

The left sidebar organizes every area of the platform. The top bar shows the
notification bell, dark-mode toggle, and your profile menu. Breadcrumbs show where
you are at all times.

| Menu item | What it does |
|-----------|--------------|
| Dashboard | Overview cards, active alerts, live WebSocket indicator |
| Disasters | Report a disaster and browse the list |
| AI Insights | Run damage/risk/priority analysis and recommendations |
| Maps | Interactive map with disaster/drone markers and layer switcher |
| Real-time Monitoring | Live overview, heatmap, distances/ETAs, drone tracking |
| Analytics | Charts for trends, types, severities, occupancy |
| Hospitals / Shelters / Volunteers / Resources / Drones | CRUD management screens |
| Rescue Teams | View teams and their members (view access) |
| Notifications | In-app alerts center |
| My Profile | Edit your details, change password, view activity |

---

## 3. Reporting a disaster

1. Go to **Disasters → Report Disaster**.
2. Fill in the form:

| Field | Notes |
|-------|-------|
| Disaster type | Flood, Earthquake, Cyclone, Wildfire, Landslide, Tsunami, Storm, Other |
| Severity | Low / Medium / High / Critical |
| Location | Address or place name |
| Coordinates | Optional latitude/longitude (or pick from map) |
| Description | What happened, casualties, infrastructure damage |
| Priority | Optional; defaults to Medium |

3. Submit. You'll receive an in-app notification and (mock) SMS/email confirmations.
4. Track your report's status from the **My Disasters** list; every status change is
   shown in the timeline and pushed live over WebSocket.

> Tip: attach photos/PDF/video evidence (max 5 files, 5 MB each) when available.

---

## 4. AI insights

Open **AI Insights** and run an analysis on any disaster scenario:

- **Damage prediction** — damage %, affected population, economic loss (₹), casualties.
- **Risk score** — 0–100 with a labelled level and an explainable factor breakdown.
- **Priority score** — 0–100 (LOW/MEDIUM/HIGH/CRITICAL) with weighted factors.
- **Recovery & response estimates** — expected days/hours with confidence bands.
- **Confidence** — overall %, input completeness, model coverage, limitations.
- **Recommendations** — ranked hospitals, shelters, volunteers, evacuation plan and
  resource quantities.

Every result carries a disclaimer and a reproducible analysis ID. The AI runs fully
**offline** — no data leaves the server.

---

## 5. Maps & real-time monitoring

- **Maps**: switch between Street, Terrain and Satellite layers. Markers are
  colour-coded by severity (Red = Critical, Orange = High, Yellow = Medium,
  Green = Low). Click a marker for a popup with details.
- **Monitoring**: view the live overview, heatmap of active incidents, distance/ETA
  calculators, and drone positions. The **WebSocket chip** (top bar) turns green when
  live updates are flowing and red when disconnected.

---

## 6. Managing entities

The Hospitals, Shelters, Volunteers, Resources and Drones screens follow the same pattern:

1. **List view** — table with search/filter and status badges.
2. **Create** — use the *Add / New* button and fill the form.
3. **Edit** — use the pencil icon on a row.
4. **Delete** — use the trash icon (requires permission; admins only for most entities).

For **Resources** you can also deploy/return stock and run maintenance cycles, and view
the movement history for auditability.

---

## 7. Notifications

- The bell in the top bar shows your **unread count**.
- Open the notification centre to read everything.
- Use *Mark all read* or mark individual entries. Notifications arrive live when the
  WebSocket is connected.

---

## 8. Profile & security

1. Open **My Profile** from the top-right avatar menu.
2. Update your name/contact details and save.
3. Change your password with the **Change password** form (current + new password).
4. Review your recent activity.

---

## 9. Public citizens (no login)

- **Report** an incident from the public reporting page (no account needed). Keep the
  generated **Report ID**.
- **Track** it later on the public tracking page with that Report ID — you'll see the
  current status and the full status timeline.

---

## 10. Troubleshooting

| Problem | What to do |
|---------|------------|
| "Account temporarily locked" | Wait 15 minutes or ask an admin to reset your account |
| "Invalid username or password" | Double-check credentials; lockout counter resets on success |
| Map tiles not loading | Check internet access (OSM tiles) — the rest of the app works offline |
| Red WebSocket chip | UI falls back to polling; reconnect is automatic |
| Data looks empty | Use the dashboard "demo data" seeds on first boot, or report a new disaster |

---

## 11. Security tips

- Never share your password or JWT token.
- Use a strong password (mix of cases, digits, symbols).
- Log out on shared machines.
- Report suspicious activity to the administrator.
