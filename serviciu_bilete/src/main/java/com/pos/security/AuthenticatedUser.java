package com.pos.security;

public record AuthenticatedUser(int userId, String role) {

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isOwnerEvent() {
        return "owner-event".equals(role);
    }

    public boolean isClient() {
        return "client".equals(role);
    }

    public boolean canManageResource(Integer resourceOwnerId) {
        if (isAdmin()) {
            return true;
        }
        if (isOwnerEvent() && resourceOwnerId != null) {
            return resourceOwnerId == userId;
        }
        return false;
    }
}
