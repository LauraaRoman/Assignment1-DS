package ro.utcluj.cs.monitoringservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SyncMessageDTO {

    @JsonProperty("type")
    private String type;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("max_hourly_consumption")
    private Double maxHourlyConsumption;

    public SyncMessageDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Double getMaxHourlyConsumption() { return maxHourlyConsumption; }
    public void setMaxHourlyConsumption(Double maxHourlyConsumption) { this.maxHourlyConsumption = maxHourlyConsumption; }
}
