package com.pos.serviciu_clienti.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val idmClientService: IdmClientService
) : OncePerRequestFilter() {

    companion object {
        const val AUTH_USER_ATTRIBUTE = "authenticatedUser"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        // Allow Swagger/OpenAPI endpoints without authentication
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header. Use: Bearer <token>")
            return
        }

        val token = authHeader.substring(7)
        val result = idmClientService.validateToken(token)

        if (!result.valid) {
            sendUnauthorizedResponse(response, result.message)
            return
        }

        // Store authenticated user in request for use in controllers
        val authenticatedUser = AuthenticatedUser(result.userId, result.role ?: "")
        request.setAttribute(AUTH_USER_ATTRIBUTE, authenticatedUser)

        filterChain.doFilter(request, response)
    }

    private fun isPublicEndpoint(path: String): Boolean {
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path == "/" ||
                path.startsWith("/webjars/") ||
                path.startsWith("/swagger-resources")
    }

    private fun sendUnauthorizedResponse(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"status":401,"error":"Unauthorized","message":"${message.replace("\"", "\\\"")}"}""")
    }
}
