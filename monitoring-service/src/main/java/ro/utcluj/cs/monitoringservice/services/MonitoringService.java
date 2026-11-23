package ro.utcluj.cs.monitoringservice.services;

import org.springframework.stereotype.Service;
import ro.utcluj.cs.monitoringservice.dtos.DeviceMeasurementDTO;
import ro.utcluj.cs.monitoringservice.dtos.HourlyConsumptionDTO;
import ro.utcluj.cs.monitoringservice.entities.DeviceReference;
import ro.utcluj.cs.monitoringservice.entities.HourlyConsumption;
import ro.utcluj.cs.monitoringservice.repositories.DeviceReferenceRepository;
import ro.utcluj.cs.monitoringservice.repositories.HourlyConsumptionRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final DeviceReferenceRepository deviceReferenceRepository;

    public MonitoringService(HourlyConsumptionRepository hourlyConsumptionRepository,
                             DeviceReferenceRepository deviceReferenceRepository) {
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.deviceReferenceRepository = deviceReferenceRepository;
    }

    public void processMeasurement(DeviceMeasurementDTO measurement) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(measurement.getTimestamp()), ZoneOffset.UTC);
        LocalDate date = dateTime.toLocalDate();
        int hour = dateTime.getHour();

        Optional<HourlyConsumption> existing = hourlyConsumptionRepository
                .findByDeviceIdAndConsumptionDateAndHour(measurement.getDeviceId(), date, hour);

        HourlyConsumption record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setTotalKwh(round(record.getTotalKwh() + measurement.getMeasurementValue()));
        } else {
            record = new HourlyConsumption(
                    measurement.getDeviceId(), date, hour,
                    round(measurement.getMeasurementValue()));
        }
        hourlyConsumptionRepository.save(record);
    }

    public void syncDevice(UUID deviceId, UUID userId, Double maxHourlyConsumption) {
        Optional<DeviceReference> existing = deviceReferenceRepository.findById(deviceId);
        if (existing.isPresent()) {
            DeviceReference ref = existing.get();
            ref.setUserId(userId);
            ref.setMaxHourlyConsumption(maxHourlyConsumption);
            deviceReferenceRepository.save(ref);
        } else {
            deviceReferenceRepository.save(new DeviceReference(deviceId, userId, maxHourlyConsumption));
        }
    }

    public List<HourlyConsumptionDTO> getConsumptionForUserAndDate(UUID userId, LocalDate date) {
        List<DeviceReference> devices = deviceReferenceRepository.findByUserId(userId);
        return devices.stream()
                .flatMap(ref -> hourlyConsumptionRepository
                        .findByDeviceIdAndConsumptionDate(ref.getId(), date)
                        .stream())
                .map(c -> new HourlyConsumptionDTO(
                        c.getId(), c.getDeviceId(), c.getConsumptionDate(), c.getHour(), c.getTotalKwh()))
                .collect(Collectors.toList());
    }

    public List<HourlyConsumptionDTO> getConsumptionForDeviceAndDate(UUID deviceId, LocalDate date) {
        return hourlyConsumptionRepository
                .findByDeviceIdAndConsumptionDate(deviceId, date)
                .stream()
                .map(c -> new HourlyConsumptionDTO(
                        c.getId(), c.getDeviceId(), c.getConsumptionDate(), c.getHour(), c.getTotalKwh()))
                .collect(Collectors.toList());
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
