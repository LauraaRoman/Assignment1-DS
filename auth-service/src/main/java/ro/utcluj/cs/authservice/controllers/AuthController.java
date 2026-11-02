package ro.utcluj.cs.authservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.utcluj.cs.authservice.entities.UserCredential;
import ro.utcluj.cs.authservice.services.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login, register and credential management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ------------------------------------------------------------------ POST /auth/register
    @Operation(
            operationId = "register",
            summary = "Register a new CLIENT user",
            description = "Creates a credential record in `auth_db`. " +
                          "The `role` field must be `CLIENT` — ADMIN registration is blocked. " +
                          "The single ADMIN account is seeded automatically at startup."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "User registered successfully"))),
            @ApiResponse(responseCode = "400", description = "Username already taken or ADMIN role requested",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Username already exists!")))
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credentials to register. `role` must be `CLIENT`.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserCredential.class),
                            examples = @ExampleObject(value = """
                                    {"username":"john_doe","password":"secret123","role":"CLIENT"}""")))
            @RequestBody UserCredential credential) {
        try {
            return ResponseEntity.ok(authService.register(credential));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ POST /auth/login
    @Operation(
            operationId = "login",
            summary = "Login",
            description = "Validates credentials against `auth_db` and returns a signed JWT token " +
                          "(HS256, 10-hour expiry). The token contains `sub` (username) and `role` claims."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT token returned as plain text",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.sig"))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "Invalid password!")))
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Parameter(description = "Username", required = true, example = "admin") @RequestParam String username,
            @Parameter(description = "Password", required = true, example = "admin123") @RequestParam String password) {
        try {
            return ResponseEntity.ok(authService.login(username, password));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ GET /auth/validate
    @Operation(
            operationId = "validateToken",
            summary = "Validate JWT token",
            description = "Internal endpoint used by NGINX `auth_request`. " +
                          "Pass the token in the `Authorization: Bearer <token>` header. " +
                          "Returns **200** when valid, **401** otherwise."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is missing, malformed, or expired")
    })
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @Parameter(description = "Bearer token — format: `Bearer <jwt>`", required = false, example = "Bearer eyJhbGci...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            if (authService.validateToken(authHeader.substring(7))) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ------------------------------------------------------------------ DELETE /auth/credentials/{username}
    @Operation(
            operationId = "deleteCredentials",
            summary = "Delete credentials by username",
            description = "Called internally by **User Service** when a user profile is deleted. " +
                          "Removes the credential record from `auth_db`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Username not found in auth_db")
    })
    @DeleteMapping("/credentials/{username}")
    public ResponseEntity<Void> deleteCredentials(
            @Parameter(description = "Username whose credentials to delete", required = true, example = "john_doe")
            @PathVariable String username) {
        try {
            authService.deleteByUsername(username);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
