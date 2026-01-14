package com.pos.security;

import com.pos.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationHelper {

    public AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {
        Object user = request.getAttribute(JwtAuthenticationFilter.AUTH_USER_ATTRIBUTE);
        if (user == null) {
            throw new ForbiddenException("User not authenticated");
        }
        return (AuthenticatedUser) user;
    }

    public void requireAdmin(HttpServletRequest request) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        if (!user.isAdmin()) {
            throw new ForbiddenException("Only administrators can perform this action");
        }
    }

    public void requireAdminOrOwner(HttpServletRequest request) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        if (!user.isAdmin() && !user.isOwnerEvent()) {
            throw new ForbiddenException("Only administrators or event owners can perform this action");
        }
    }

    public void requireOwnership(HttpServletRequest request, Integer resourceOwnerId) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        if (!user.isAdmin()) {
            if (!user.isOwnerEvent()) {
                throw new ForbiddenException("Only administrators or event owners can perform this action");
            }
            if (resourceOwnerId == null || resourceOwnerId != user.userId()) {
                throw new ForbiddenException("You can only manage your own resources");
            }
        }
    }

    public void requireOwnershipForCreate(HttpServletRequest request, Integer providedOwnerId) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        if (!user.isAdmin()) {
            if (!user.isOwnerEvent()) {
                throw new ForbiddenException("Only administrators or event owners can create resources");
            }
            // For owner-event, they must set themselves as the owner
            if (providedOwnerId != null && providedOwnerId != user.userId()) {
                throw new ForbiddenException("You can only create resources owned by yourself");
            }
        }
    }

    public int getEffectiveOwnerId(HttpServletRequest request, Integer providedOwnerId) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        if (user.isAdmin() && providedOwnerId != null) {
            return providedOwnerId;
        }
        return user.userId();
    }
}
