-- =====================================================================
-- V2: Public citizen reporting support
-- Adds fields used by the no-login public report flow and the generated
-- Report ID used for tracking. Aligned with Disaster entity (Hibernate
-- validate). Safe on both H2 (PostgreSQL mode) and PostgreSQL.
-- =====================================================================

ALTER TABLE disasters ADD COLUMN report_id VARCHAR(50);
ALTER TABLE disasters ADD COLUMN reporter_name VARCHAR(255);
ALTER TABLE disasters ADD COLUMN reporter_mobile VARCHAR(50);
ALTER TABLE disasters ADD COLUMN reporter_email VARCHAR(255);
ALTER TABLE disasters ADD COLUMN address VARCHAR(500);
ALTER TABLE disasters ADD COLUMN image_urls VARCHAR(10000);
ALTER TABLE disasters ADD COLUMN video_urls VARCHAR(10000);
ALTER TABLE disasters ADD COLUMN source VARCHAR(20) DEFAULT 'SYSTEM';

CREATE UNIQUE INDEX uk_disasters_report_id ON disasters (report_id);
