package com.disaster.security;

import com.disaster.entity.Role;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.disaster.security.Permissions.*;

/**
 * Central Role-Based Access Control (RBAC) policy.
 *
 * Maps every {@link Role} to the set of {@link Permissions} it is allowed to
 * exercise. Used both by Spring method security
 * ({@code @PreAuthorize("@rbacService.hasPermission(...)")}) and by the
 * frontend authorization layer (permissions are returned in the login payload).
 */
@Component("rbacService")
public class RbacService {

    private static final Set<String> ALL_PERMISSIONS = Set.of(
            DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
            AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW, CASE_STUDY_MANAGE,
            DISASTER_VIEW, DISASTER_CREATE, DISASTER_UPDATE, DISASTER_DELETE,
            USER_VIEW, USER_MANAGE, AUDIT_VIEW, SETTINGS_MANAGE, EXPORT_VIEW, ANALYTICS_VIEW,
            HOSPITAL_VIEW, HOSPITAL_MANAGE,
            SHELTER_VIEW, SHELTER_MANAGE,
            VOLUNTEER_VIEW, VOLUNTEER_MANAGE,
            RESOURCE_VIEW, RESOURCE_MANAGE,
            DRONE_VIEW, DRONE_MANAGE,
            RESCUE_TEAM_VIEW, RESCUE_TEAM_MANAGE);

    private static final Map<Role, Set<String>> MATRIX = new EnumMap<>(Role.class);

    static {
        // --- ADMIN: unrestricted access to every module ---
        MATRIX.put(Role.ADMIN, ALL_PERMISSIONS);

        // --- Disaster Management Officer (command & control) ---
        MATRIX.put(Role.DMO, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW, CASE_STUDY_MANAGE,
                DISASTER_VIEW, DISASTER_CREATE, DISASTER_UPDATE, DISASTER_DELETE,
                USER_VIEW, EXPORT_VIEW, ANALYTICS_VIEW,
                HOSPITAL_VIEW, SHELTER_VIEW, VOLUNTEER_VIEW,
                RESOURCE_VIEW, RESOURCE_MANAGE, DRONE_VIEW,
                RESCUE_TEAM_VIEW, RESCUE_TEAM_MANAGE));

        // --- Police ---
        MATRIX.put(Role.POLICE, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW, DISASTER_CREATE, DISASTER_UPDATE,
                RESOURCE_VIEW, RESCUE_TEAM_VIEW));

        // --- Fire Department ---
        MATRIX.put(Role.FIRE_DEPARTMENT, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW, DISASTER_CREATE, DISASTER_UPDATE,
                RESOURCE_VIEW, RESCUE_TEAM_VIEW));

        // --- Hospital Staff ---
        MATRIX.put(Role.HOSPITAL_STAFF, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW,
                HOSPITAL_VIEW, HOSPITAL_MANAGE,
                RESOURCE_VIEW, RESCUE_TEAM_VIEW));

        // --- Rescue Team ---
        MATRIX.put(Role.RESCUE_TEAM, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW,
                RESCUE_TEAM_VIEW, RESOURCE_VIEW, DRONE_VIEW));

        // --- Volunteer Coordinator ---
        MATRIX.put(Role.VOLUNTEER_COORDINATOR, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW,
                VOLUNTEER_VIEW, VOLUNTEER_MANAGE,
                SHELTER_VIEW));

        // --- NGO Coordinator ---
        MATRIX.put(Role.NGO_COORDINATOR, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW,
                SHELTER_VIEW, SHELTER_MANAGE,
                VOLUNTEER_VIEW));

        // --- Legacy citizen account ---
        MATRIX.put(Role.USER, Set.of(
                DASHBOARD_VIEW, PROFILE_VIEW, NOTIFICATION_VIEW, MAP_VIEW, WEATHER_VIEW,
                AI_VIEW, ROUTE_VIEW, CASE_STUDY_VIEW,
                DISASTER_VIEW, DISASTER_CREATE));
    }

    /**
     * Permissions granted to a role. Never returns {@code null}.
     */
    public Set<String> permissionsForRole(Role role) {
        if (role == null) return Set.of();
        return MATRIX.getOrDefault(role, Set.of());
    }

    /**
     * Whether the supplied principal (a {@link UserDetailsImpl} from the Spring
     * Security context) holds the given permission.
     */
    public boolean hasPermission(Object principal, String permission) {
        if (!(principal instanceof UserDetailsImpl user)) return false;
        return permissionsForRole(user.getRole()).contains(permission);
    }

    /**
     * Convenience guard used by SpEL for granular read/write checks.
     */
    public boolean hasAnyPermission(Object principal, String... permissions) {
        if (!(principal instanceof UserDetailsImpl user)) return false;
        Set<String> granted = permissionsForRole(user.getRole());
        for (String p : permissions) {
            if (granted.contains(p)) return true;
        }
        return false;
    }
}
