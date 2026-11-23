
package ro.utcluj.cs.monitoringservice.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "hourly_consumption",
        uniqueConstraints = @UniqueConstraint(columnNames = {"device_id", "consumption_date", "hour"}))
public class HourlyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "consumption_date", nullable = false)
    private LocalDate consumptionDate;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private double totalKwh;

    public HourlyConsumption() {}

    public HourlyConsumption(UUID deviceId, LocalDate consumptionDate, int hour, double totalKwh) {
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
