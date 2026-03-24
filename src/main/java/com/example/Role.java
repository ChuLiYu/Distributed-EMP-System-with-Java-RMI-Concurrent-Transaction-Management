package com.example;

/**
 * Role enum for RBAC (Role-Based Access Control)
 * 
 * Per FINTECH_REQUIREMENTS.md Section 5:
 * - Viewer: Can read core data only
 * - Operator: Can read, create, update
 * - Supervisor: Can read, create, update, delete + view audit (read-only)
 * - Admin: Full access including audit
 */
public enum Role {
    VIEWER("Viewer"),
    OPERATOR("Operator"),
    SUPERVISOR("Supervisor"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if role has read permission
     */
    public boolean canRead() {
        return true; // All roles can read
    }

    /**
     * Check if role has create permission
     */
    public boolean canCreate() {
        return this == OPERATOR || this == SUPERVISOR || this == ADMIN;
    }

    /**
     * Check if role has update permission
     */
    public boolean canUpdate() {
        return this == OPERATOR || this == SUPERVISOR || this == ADMIN;
    }

    /**
     * Check if role has delete permission
     */
    public boolean canDelete() {
        return this == SUPERVISOR || this == ADMIN;
    }

    /**
     * Check if role can view audit logs
     */
    public boolean canViewAudit() {
        return this == SUPERVISOR || this == ADMIN;
    }
}
