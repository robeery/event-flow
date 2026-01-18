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

    /**
     * Requires the user to be authenticated (any role is allowed).
     * Used for operations that any logged-in user can perform, like buying tickets.
     */
    public void requireAuthenticated(HttpServletRequest request) {
        getAuthenticatedUser(request); // Throws if not authenticated
    }

    /**
     * Checks if user can manage a ticket (buy/return).
     * Admins and owner-events can manage any ticket.
     * Clients can only buy/return tickets (no ownership check for buying).
     */
    public void requireCanPurchaseTicket(HttpServletRequest request) {
        AuthenticatedUser user = getAuthenticatedUser(request);
        // Any authenticated user can purchase tickets
        if (!user.isAdmin() && !user.isOwnerEvent() && !user.isClient()) {
            throw new ForbiddenException("You must be logged in to purchase tickets");
        }
    }
}
