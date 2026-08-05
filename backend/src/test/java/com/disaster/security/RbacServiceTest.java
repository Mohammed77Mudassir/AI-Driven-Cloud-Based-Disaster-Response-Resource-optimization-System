package com.disaster.security;

import com.disaster.entity.Role;
import org.junit.jupiter.api.Test;

import static com.disaster.security.Permissions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RBAC permission matrix: administrative access, role
 * separation of duties, and safe handling of null/unknown principals.
 */
class RbacServiceTest {

    private final RbacService rbacService = new RbacService();

    private UserDetailsImpl user(Role role) {
        return new UserDetailsImpl(1L, "user", "user@example.com", "pw", role,
                true, java.util.List.of());
    }

    @Test
    void adminHasEveryPermission() {
        for (String permission : rbacService.permissionsForRole(Role.ADMIN)) {
            assertTrue(rbacService.hasPermission(user(Role.ADMIN), permission));
        }
    }

    @Test
    void adminCanManageEveryModule() {
        assertTrue(rbacService.hasPermission(user(Role.ADMIN), USER_MANAGE));
        assertTrue(rbacService.hasPermission(user(Role.ADMIN), SETTINGS_MANAGE));
        assertTrue(rbacService.hasPermission(user(Role.ADMIN), AUDIT_VIEW));
        assertTrue(rbacService.hasPermission(user(Role.ADMIN), DISASTER_DELETE));
    }

    @Test
    void regularUserCannotEscalate() {
        assertFalse(rbacService.hasPermission(user(Role.USER), USER_MANAGE));
        assertFalse(rbacService.hasPermission(user(Role.USER), DISASTER_DELETE));
        assertFalse(rbacService.hasPermission(user(Role.USER), AUDIT_VIEW));
        assertFalse(rbacService.hasPermission(user(Role.USER), SETTINGS_MANAGE));
    }

    @Test
    void regularUserCanReportDisasters() {
        assertTrue(rbacService.hasPermission(user(Role.USER), DISASTER_CREATE));
        assertTrue(rbacService.hasPermission(user(Role.USER), DISASTER_VIEW));
    }

    @Test
    void dmoHasOperationalControlButNotUserManagement() {
        UserDetailsImpl dmo = user(Role.DMO);
        assertTrue(rbacService.hasPermission(dmo, DISASTER_CREATE));
        assertTrue(rbacService.hasPermission(dmo, DISASTER_UPDATE));
        assertTrue(rbacService.hasPermission(dmo, RESCUE_TEAM_MANAGE));
        assertFalse(rbacService.hasPermission(dmo, USER_MANAGE));
        assertFalse(rbacService.hasPermission(dmo, SETTINGS_MANAGE));
    }

    @Test
    void hospitalStaffCannotModifyNonHospitalResources() {
        UserDetailsImpl staff = user(Role.HOSPITAL_STAFF);
        assertTrue(rbacService.hasPermission(staff, HOSPITAL_MANAGE));
        assertFalse(rbacService.hasPermission(staff, RESOURCE_MANAGE));
        assertFalse(rbacService.hasPermission(staff, DISASTER_UPDATE));
    }

    @Test
    void nonUserPrincipalIsDenied() {
        assertFalse(rbacService.hasPermission(new Object(), DISASTER_VIEW));
        assertFalse(rbacService.hasPermission(null, DISASTER_VIEW));
    }

    @Test
    void nullRoleReturnsEmptyPermissions() {
        assertTrue(rbacService.permissionsForRole(null).isEmpty());
    }

    @Test
    void hasAnyPermissionShortCircuits() {
        UserDetailsImpl user = user(Role.USER);
        assertTrue(rbacService.hasAnyPermission(user, "SOME_UNKNOWN_PERM", DISASTER_CREATE));
        assertFalse(rbacService.hasAnyPermission(user, "SOME_UNKNOWN_PERM", USER_MANAGE));
    }
}
