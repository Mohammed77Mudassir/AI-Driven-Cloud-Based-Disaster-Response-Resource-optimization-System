-- =====================================================================
-- V7: Performance indexes
--
-- Supports nearest-neighbour AI recommendations (hospitals, shelters,
-- volunteers, resources) and common list/filter queries.
-- Compatible with both H2 (PostgreSQL mode) and PostgreSQL.
-- =====================================================================

-- Hospitals: AI hospital recommendation engine scans by proximity.
CREATE INDEX idx_hospitals_geo ON hospitals (latitude, longitude);

-- Shelters: AI shelter recommendation engine scans by proximity.
CREATE INDEX idx_shelters_geo ON shelters (latitude, longitude);

-- Volunteers: proximity matching for volunteer dispatch.
CREATE INDEX idx_volunteers_geo ON volunteers (latitude, longitude);
CREATE INDEX idx_volunteers_available ON volunteers (available);

-- Resources: proximity matching for resource optimization.
CREATE INDEX idx_resources_geo ON resources (latitude, longitude);
CREATE INDEX idx_resources_available ON resources (available);

-- Rescue teams: proximity matching for deployment.
CREATE INDEX idx_rescue_teams_geo ON rescue_teams (latitude, longitude);
CREATE INDEX idx_rescue_teams_status ON rescue_teams (status);

-- Drones: proximity matching for live tracking.
CREATE INDEX idx_drones_geo ON drones (latitude, longitude);

-- Cases: common filters used by the case-study analysis module.
CREATE INDEX idx_case_studies_type ON case_studies (disaster_type);
CREATE INDEX idx_case_studies_year ON case_studies (disaster_year);

-- Mission/team member lookups by availability flag.
CREATE INDEX idx_team_members_available ON team_members (available);
