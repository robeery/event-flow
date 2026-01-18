package com.pos.controller;

import com.pos.security.IdmClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for login/logout")
public class AuthController {

    private final IdmClientService idmClientService;

    @Operation(summary = "Login", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        IdmClientService.AuthResult result = idmClientService.authenticate(request.email(), request.password());

        if (result.success()) {
            // Validăm token-ul pentru a obține informații despre user
            IdmClientService.TokenValidationResult validation = idmClientService.validateToken(result.token());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", result.token(),
                    "userId", validation.userId(),
                    "role", validation.role(),
                    "message", result.message()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", result.message()
            ));
        }
    }

    @Operation(summary = "Logout", description = "Invalidate the current JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing or invalid Authorization header"
            ));
        }

        String token = authHeader.substring(7);
        IdmClientService.InvalidateResult result = idmClientService.invalidateToken(token);

        return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "message", result.message()
        ));
    }

    public record LoginRequest(String email, String password) {}
}
