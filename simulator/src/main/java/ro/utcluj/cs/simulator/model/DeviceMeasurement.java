package ro.utcluj.cs.simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DeviceMeasurement {

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("device_id")
    private UUID deviceId;

    @JsonProperty("measurement_value")
    private double measurementValue;

    public DeviceMeasurement(long timestamp, UUID deviceId, double measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public long getTimestamp() { return timestamp; }
    public UUID getDeviceId() { return deviceId; }
    public double getMeasurementValue() { return measurementValue; }
}
