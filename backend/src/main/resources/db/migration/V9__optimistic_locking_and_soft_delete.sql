-- =====================================================================
-- V9: Optimistic locking + soft delete
--
-- - Adds a version column to resources so concurrent deploy/return/
--   maintenance operations are protected against lost updates.
-- - Adds a soft-delete flag to disasters so historical reports are
--   retained for audit purposes while being excluded from all queries
--   (enforced at the ORM level via @SQLRestriction).
-- - Covers the remaining child-table lookups and the dominant
--   (status-filtered, date-sorted) disaster list query.
-- Compatible with both H2 (PostgreSQL mode) and PostgreSQL.
-- =====================================================================

-- Optimistic locking for concurrent resource operations.
ALTER TABLE resources ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Soft-delete flag: reports are retained for audit, excluded from queries.
ALTER TABLE disasters ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Dominant list query: filter by status, sort by date.
CREATE INDEX idx_disasters_status_date ON disasters (status, date);

-- Child-table lookups not yet covered by V7/V8.
CREATE INDEX idx_comments_author ON disaster_comments (author_id);
CREATE INDEX idx_assignments_team ON disaster_assignments (team_id);
CREATE INDEX idx_status_timeline_disaster ON status_timeline (disaster_id);
