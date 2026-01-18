package com.pos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_USER_ATTRIBUTE = "authenticatedUser";

    @Autowired
    private IdmClientService idmClientService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow Swagger/OpenAPI endpoints without authentication
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // GET requests are public - but still parse token if provided
        if ("GET".equalsIgnoreCase(method)) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                IdmClientService.TokenValidationResult result = idmClientService.validateToken(token);
                if (result.valid()) {
                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(result.userId(), result.role());
                    request.setAttribute(AUTH_USER_ATTRIBUTE, authenticatedUser);
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // POST, PUT, DELETE, PATCH require authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header. Use: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7);

        IdmClientService.TokenValidationResult result = idmClientService.validateToken(token);

        if (!result.valid()) {
            sendUnauthorizedResponse(response, result.message());
            return;
        }

        // Store authenticated user in request for use in controllers
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(result.userId(), result.role());
        request.setAttribute(AUTH_USER_ATTRIBUTE, authenticatedUser);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/api/auth");
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                message.replace("\"", "\\\"")
        ));
    }
}
