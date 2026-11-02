package ro.utcluj.cs.userservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ro.utcluj.cs.userservice.dtos.validators.ValidRole;

@Schema(name = "UserInsertDTO", description = "Payload for creating or updating a user profile")
public class UserInsertDTO {

    @NotNull(message = "Username cannot be null")
    @Schema(description = "Unique username", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotNull(message = "Password cannot be null")
    @Schema(description = "Plain-text password (not hashed in user-service)", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @ValidRole
    @Schema(description = "Role — ADMIN or CLIENT", example = "CLIENT", allowableValues = {"ADMIN", "CLIENT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    public UserInsertDTO() {}
    public UserInsertDTO(String username, String password, String role) {
        this.username = username; this.password = password; this.role = role;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
