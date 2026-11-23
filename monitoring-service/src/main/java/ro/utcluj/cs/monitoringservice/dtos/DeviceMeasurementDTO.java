package ro.utcluj.cs.monitoringservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DeviceMeasurementDTO {

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("device_id")
    private UUID deviceId;

    @JsonProperty("measurement_value")
    private double measurementValue;

    public DeviceMeasurementDTO() {}

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public double getMeasurementValue() { return measurementValue; }
    public void setMeasurementValue(double measurementValue) { this.measurementValue = measurementValue; }
}
