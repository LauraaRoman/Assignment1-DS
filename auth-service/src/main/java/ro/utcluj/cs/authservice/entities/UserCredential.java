package ro.utcluj.cs.authservice.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "credentials")
@Data
@Schema(name = "UserCredential", description = "Credentials used for authentication")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Auto-generated UUID", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Unique username", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Column(nullable = false)
    @Schema(description = "Plain-text password (stored hashed)", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Column(nullable = false)
    @Schema(description = "Role — only CLIENT is accepted via register; ADMIN is seeded at startup", example = "CLIENT", allowableValues = {"CLIENT", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}
