package ro.utcluj.cs.monitoringservice.dtos;

import java.time.LocalDate;
import java.util.UUID;

public class HourlyConsumptionDTO {
    private UUID id;
    private UUID deviceId;
    private LocalDate consumptionDate;
    private int hour;
    private double totalKwh;

    public HourlyConsumptionDTO() {}

    public HourlyConsumptionDTO(UUID id, UUID deviceId, LocalDate consumptionDate, int hour, double totalKwh) {
        this.id = id;
        this.deviceId = deviceId;
        this.consumptionDate = consumptionDate;
        this.hour = hour;
        this.totalKwh = totalKwh;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public LocalDate getConsumptionDate() { return consumptionDate; }
    public void setConsumptionDate(LocalDate consumptionDate) { this.consumptionDate = consumptionDate; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(double totalKwh) { this.totalKwh = totalKwh; }
}
