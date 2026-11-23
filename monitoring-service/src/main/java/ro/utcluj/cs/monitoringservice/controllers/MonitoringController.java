package ro.utcluj.cs.monitoringservice.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.utcluj.cs.monitoringservice.dtos.HourlyConsumptionDTO;
import ro.utcluj.cs.monitoringservice.services.MonitoringService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HourlyConsumptionDTO>> getConsumptionForUser(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(monitoringService.getConsumptionForUserAndDate(userId, date));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<HourlyConsumptionDTO>> getConsumptionForDevice(
            @PathVariable UUID deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(monitoringService.getConsumptionForDeviceAndDate(deviceId, date));
    }
}
