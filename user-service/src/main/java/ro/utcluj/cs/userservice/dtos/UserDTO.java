package ro.utcluj.cs.userservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "UserDTO", description = "User profile returned by the API")
public class UserDTO {

    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Unique username", example = "john_doe")
    private String username;

    @Schema(description = "User role", example = "CLIENT", allowableValues = {"ADMIN", "CLIENT"})
    private String role;

    public UserDTO(UUID id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public void setId(UUID id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
}
