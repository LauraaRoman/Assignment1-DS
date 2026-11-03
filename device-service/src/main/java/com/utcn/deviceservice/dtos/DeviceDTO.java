package com.utcn.deviceservice.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "DeviceDTO", description = "Smart device returned by the API")
public class DeviceDTO {

    @Schema(description = "Unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Short description of the device", example = "Smart thermostat")
    private String description;

    @Schema(description = "Physical or logical address", example = "Room 101, Building A")
    private String address;

    @Schema(description = "Maximum hourly energy consumption in kWh", example = "1.5")
    private double maxHourlyEnergyConsumption;

    @Schema(description = "UUID of the user this device is assigned to", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String description, String address, double maxHourlyEnergyConsumption, UUID userId) {
        this.id = id; this.description = description; this.address = address;
        this.maxHourlyEnergyConsumption = maxHourlyEnergyConsumption; this.userId = userId;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getMaxHourlyEnergyConsumption() { return maxHourlyEnergyConsumption; }
    public void setMaxHourlyEnergyConsumption(double maxHourlyEnergyConsumption) { this.maxHourlyEnergyConsumption = maxHourlyEnergyConsumption; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
