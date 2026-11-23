package ro.utcluj.cs.monitoringservice.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "device_references")
public class DeviceReference {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "max_hourly_consumption")
    private Double maxHourlyConsumption;

    public DeviceReference() {}

    public DeviceReference(UUID id, UUID userId, Double maxHourlyConsumption) {
        this.id = id;
        this.userId = userId;
        this.maxHourlyConsumption = maxHourlyConsumption;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Double getMaxHourlyConsumption() { return maxHourlyConsumption; }
    public void setMaxHourlyConsumption(Double maxHourlyConsumption) { this.maxHourlyConsumption = maxHourlyConsumption; }
}
