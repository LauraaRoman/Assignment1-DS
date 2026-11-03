package com.utcn.deviceservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

@Schema(name = "DeviceInsertDTO", description = "Payload for creating or updating a smart device")
public class DeviceInsertDTO {

    @NotBlank(message = "Description is mandatory")
    @Schema(description = "Short description of the device", example = "Smart thermostat", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotBlank(message = "Address is mandatory")
    @Schema(description = "Physical or logical address", example = "Room 101, Building A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotNull(message = "Maximum hourly energy consumption is mandatory")
    @Positive(message = "Energy consumption must be greater than zero")
    @Schema(description = "Maximum hourly energy consumption in kWh — must be > 0", example = "1.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private double maxHourlyEnergyConsumption;

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "UUID of the user this device is assigned to", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;

    public DeviceInsertDTO() {}
    public DeviceInsertDTO(String description, String address, double maxHourlyEnergyConsumption, UUID userId) {
        this.description = description; this.address = address;
        this.maxHourlyEnergyConsumption = maxHourlyEnergyConsumption; this.userId = userId;
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getMaxHourlyEnergyConsumption() { return maxHourlyEnergyConsumption; }
    public void setMaxHourlyEnergyConsumption(double v) { this.maxHourlyEnergyConsumption = v; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
