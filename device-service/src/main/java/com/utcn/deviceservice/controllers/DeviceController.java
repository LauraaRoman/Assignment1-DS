package com.utcn.deviceservice.controllers;

import com.utcn.deviceservice.dtos.DeviceDTO;
import com.utcn.deviceservice.dtos.DeviceInsertDTO;
import com.utcn.deviceservice.entities.UserReference;
import com.utcn.deviceservice.repositories.UserReferenceRepository;
import com.utcn.deviceservice.services.DeviceService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@CrossOrigin
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Device Management", description = "CRUD operations for smart devices stored in device_db")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    private UserReferenceRepository userReferenceRepository;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // ------------------------------------------------------------------ POST /devices/sync-user
    @Operation(operationId = "syncUser", summary = "Sync user ID (internal)",
            description = "Called internally by **User Service** after creating a user profile. " +
                          "Registers the user UUID in `device_db` so devices can reference it. " +
                          "Not intended for direct client use.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User ID recorded successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @PostMapping("/sync-user")
    public ResponseEntity<Void> syncUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "UUID of the newly created user",
                    required = true,
                    content = @Content(schema = @Schema(type = "string", format = "uuid"),
                            examples = @ExampleObject(value = "\"550e8400-e29b-41d4-a716-446655440000\"")))
            @RequestBody UUID userId) {
        userReferenceRepository.save(new UserReference(userId));
        return ResponseEntity.ok().build();
    }

    // ------------------------------------------------------------------ GET /devices
    @Operation(operationId = "listDevices", summary = "List all devices",
            description = "Returns every smart device in the system. Requires a valid JWT (any role).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of devices",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return new ResponseEntity<>(deviceService.findDevices(), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ GET /devices/{id}
    @Operation(operationId = "getDeviceById", summary = "Get device by ID",
            description = "Returns a single device by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getDevice(
            @Parameter(description = "Device UUID", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable UUID id) {
        return new ResponseEntity<>(deviceService.findDeviceById(id), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ GET /devices/user/{userId}
    @Operation(operationId = "getDevicesByUser", summary = "List devices for a user",
            description = "Returns all devices assigned to the given user UUID. " +
                          "Used by the CLIENT role to display their own devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of devices belonging to the user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUserId(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }

    // ------------------------------------------------------------------ POST /devices
    @Operation(operationId = "createDevice", summary = "Create device",
            description = "Creates a new smart device and assigns it to a user. " +
                          "The `userId` must already exist in `device_db` (synced from User Service).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed (missing/invalid fields)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DeviceDTO> insertDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceInsertDTO.class),
                            examples = @ExampleObject(value = """
                                    {"description":"Smart thermostat","address":"Room 101","maxHourlyEnergyConsumption":1.5,"userId":"550e8400-e29b-41d4-a716-446655440000"}""")))
            @Valid @RequestBody DeviceInsertDTO deviceDTO) {
        return new ResponseEntity<>(deviceService.insert(deviceDTO), HttpStatus.CREATED);
    }

    // ------------------------------------------------------------------ PUT /devices/{id}
    @Operation(operationId = "updateDevice", summary = "Update device",
            description = "Replaces all fields of an existing device. The `userId` can be changed to reassign the device to a different user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated device",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(
            @Parameter(description = "Device UUID", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New device data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceInsertDTO.class),
                            examples = @ExampleObject(value = """
                                    {"description":"Updated thermostat","address":"Room 202","maxHourlyEnergyConsumption":2.0,"userId":"550e8400-e29b-41d4-a716-446655440000"}""")))
            @Valid @RequestBody DeviceInsertDTO deviceDTO) {
        return new ResponseEntity<>(deviceService.update(id, deviceDTO), HttpStatus.OK);
    }

    // ------------------------------------------------------------------ DELETE /devices/{id}
    @Operation(operationId = "deleteDevice", summary = "Delete device",
            description = "Permanently removes a single device from `device_db`.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device UUID", required = true, example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable UUID id) {
        deviceService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ------------------------------------------------------------------ DELETE /devices/user/{userId}
    @Operation(operationId = "deleteDevicesByUser", summary = "Delete all devices for a user",
            description = "Removes every device assigned to the given user UUID. " +
                          "Called internally by **User Service** when a user account is deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All user devices deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content)
    })
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteDevicesByUser(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId) {
        deviceService.deleteDevicesByUserId(userId);
        return ResponseEntity.ok().build();
    }
}
