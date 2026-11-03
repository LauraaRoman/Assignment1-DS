package ro.utcluj.cs.userservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.utcluj.cs.userservice.dtos.UserDTO;
import ro.utcluj.cs.userservice.dtos.UserInsertDTO;
import ro.utcluj.cs.userservice.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "CRUD operations for user profiles stored in user_db")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------------------------------ GET /users
    @Operation(operationId = "listUsers", summary = "List all users",
            description = "Returns every user profile. Requires a valid JWT token (any role).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        return new ResponseEntity<>(userService.findUsers(), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ GET /users/{id}
    @Operation(operationId = "getUserById", summary = "Get user by ID",
            description = "Returns a single user profile by UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ GET /users/by-username/{username}
    @Operation(operationId = "getUserByUsername", summary = "Get user by username",
            description = "Returns a single user profile by username. Used by the frontend to resolve the current user's UUID from the JWT `sub` claim.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Username not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "Username", required = true, example = "john_doe")
            @PathVariable String username) {
        return new ResponseEntity<>(userService.findByUsername(username), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ POST /users
    @Operation(operationId = "createUser", summary = "Create user",
            description = "Creates a new user profile and synchronises the generated UUID to **Device Service** " +
                          "so devices can be assigned to this user. Creation is rolled back if the sync fails.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created — returns the new UUID",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "string", format = "uuid"),
                            examples = @ExampleObject(value = "\"550e8400-e29b-41d4-a716-446655440000\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing/invalid fields)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Device Service sync failed", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UUID> insertUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserInsertDTO.class),
                            examples = @ExampleObject(value = """
                                    {"username":"john_doe","password":"secret123","role":"CLIENT"}""")))
            @Valid @RequestBody UserInsertDTO userDTO) {
        return new ResponseEntity<>(userService.insert(userDTO).getId(), HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------ PUT /users/{id}
    @Operation(operationId = "updateUser", summary = "Update user",
            description = "Replaces `username`, `password`, and `role` of an existing user. " +
                          "Requires ADMIN role in practice (enforced at gateway level).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated user profile",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New user data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserInsertDTO.class),
                            examples = @ExampleObject(value = """
                                    {"username":"john_updated","password":"newpass","role":"CLIENT"}""")))
            @Valid @RequestBody UserInsertDTO userDTO) {
        return new ResponseEntity<>(userService.update(id, userDTO), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ DELETE /users/{id}
    @Operation(operationId = "deleteUser", summary = "Delete user",
            description = "Deletes a user profile, cascades deletion of all their devices in **Device Service**, " +
                          "and removes their credentials from **Auth Service**.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
