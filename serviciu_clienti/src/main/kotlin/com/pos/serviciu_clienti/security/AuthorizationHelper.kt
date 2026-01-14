package com.pos.serviciu_clienti.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class AuthorizationHelper {

    fun getAuthenticatedUser(request: HttpServletRequest): AuthenticatedUser {
        val user = request.getAttribute(JwtAuthenticationFilter.AUTH_USER_ATTRIBUTE)
            ?: throw ForbiddenException("User not authenticated")
        return user as AuthenticatedUser
    }

    fun requireAdmin(request: HttpServletRequest) {
        val user = getAuthenticatedUser(request)
        if (!user.isAdmin()) {
            throw ForbiddenException("Only administrators can perform this action")
        }
    }

    fun requireAdminOrClient(request: HttpServletRequest) {
        val user = getAuthenticatedUser(request)
        if (!user.isAdmin() && !user.isClient()) {
            throw ForbiddenException("Only administrators or clients can perform this action")
        }
    }

    fun requireClientOwnership(request: HttpServletRequest, clientIdmUserId: Int?) {
        val user = getAuthenticatedUser(request)
        if (!user.isAdmin()) {
            if (!user.isClient()) {
                throw ForbiddenException("Only administrators or clients can perform this action")
            }
            if (clientIdmUserId == null || clientIdmUserId != user.userId) {
                throw ForbiddenException("You can only manage your own client profile")
            }
        }
    }

    fun getUserIdForNewClient(request: HttpServletRequest): Int {
        val user = getAuthenticatedUser(request)
        return user.userId
    }
}
