package com.disaster.entity;

/**
 * System roles for the disaster management platform.
 *
 * ADMIN                 - Full access to every module.
 * DMO                   - Disaster Management Officer (command & control).
 * POLICE                - Law enforcement responder.
 * FIRE_DEPARTMENT       - Fire & emergency response.
 * HOSPITAL_STAFF        - Medical facilities staff.
 * RESCUE_TEAM           - Field rescue personnel.
 * VOLUNTEER_COORDINATOR - Volunteer deployment coordinator.
 * NGO_COORDINATOR       - NGO / relief organisation coordinator.
 * USER                  - Legacy self-registered citizen account (kept for
 *                         backward compatibility; public citizens normally do
 *                         not need an account and use the public report flow).
 */
public enum Role {
    ADMIN,
    DMO,
    POLICE,
    FIRE_DEPARTMENT,
    HOSPITAL_STAFF,
    RESCUE_TEAM,
    VOLUNTEER_COORDINATOR,
    NGO_COORDINATOR,
    USER
}
