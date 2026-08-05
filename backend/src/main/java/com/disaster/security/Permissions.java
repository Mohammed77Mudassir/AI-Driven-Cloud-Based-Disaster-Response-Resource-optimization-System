package com.disaster.security;

/**
 * Canonical permission identifiers used for Role-Based Access Control (RBAC).
 * A permission grants the ability to perform a specific capability (read or
 * mutate) on a module. Roles are mapped to a set of permissions in
 * {@link RbacService}.
 */
public final class Permissions {

    private Permissions() {}

    // Core
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    public static final String PROFILE_VIEW = "PROFILE_VIEW";
    public static final String NOTIFICATION_VIEW = "NOTIFICATION_VIEW";
    public static final String MAP_VIEW = "MAP_VIEW";
    public static final String WEATHER_VIEW = "WEATHER_VIEW";

    // AI
    public static final String AI_VIEW = "AI_VIEW";
    public static final String ROUTE_VIEW = "ROUTE_VIEW";
    public static final String CASE_STUDY_VIEW = "CASE_STUDY_VIEW";
    public static final String CASE_STUDY_MANAGE = "CASE_STUDY_MANAGE";

    // Disasters
    public static final String DISASTER_VIEW = "DISASTER_VIEW";
    public static final String DISASTER_CREATE = "DISASTER_CREATE";
    public static final String DISASTER_UPDATE = "DISASTER_UPDATE";
    public static final String DISASTER_DELETE = "DISASTER_DELETE";

    // Users & Administration
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String AUDIT_VIEW = "AUDIT_VIEW";
    public static final String SETTINGS_MANAGE = "SETTINGS_MANAGE";
    public static final String EXPORT_VIEW = "EXPORT_VIEW";
    public static final String ANALYTICS_VIEW = "ANALYTICS_VIEW";

    // Operational entities
    public static final String HOSPITAL_VIEW = "HOSPITAL_VIEW";
    public static final String HOSPITAL_MANAGE = "HOSPITAL_MANAGE";
    public static final String SHELTER_VIEW = "SHELTER_VIEW";
    public static final String SHELTER_MANAGE = "SHELTER_MANAGE";
    public static final String VOLUNTEER_VIEW = "VOLUNTEER_VIEW";
    public static final String VOLUNTEER_MANAGE = "VOLUNTEER_MANAGE";
    public static final String RESOURCE_VIEW = "RESOURCE_VIEW";
    public static final String RESOURCE_MANAGE = "RESOURCE_MANAGE";
    public static final String DRONE_VIEW = "DRONE_VIEW";
    public static final String DRONE_MANAGE = "DRONE_MANAGE";
    public static final String RESCUE_TEAM_VIEW = "RESCUE_TEAM_VIEW";
    public static final String RESCUE_TEAM_MANAGE = "RESCUE_TEAM_MANAGE";
}
