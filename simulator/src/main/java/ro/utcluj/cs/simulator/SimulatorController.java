package ro.utcluj.cs.simulator;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/simulate")
public class SimulatorController {

    private final SimulatorScheduler scheduler;

    public SimulatorController(SimulatorScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/backfill")
    public ResponseEntity<Map<String, Object>> backfill(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = date != null ? date : LocalDate.now();
        List<UUID> devices = scheduler.resolveDeviceIds();

        if (devices.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No devices found. Pass --simulator.device-ids=<uuid> or ensure device-service is reachable."));
        }

        scheduler.backfillDay(target);
        return ResponseEntity.ok(Map.of(
                "date", target.toString(),
                "devices", devices.size(),
                "messagesPerDevice", 144,
                "total", devices.size() * 144
        ));
    }

    @PostMapping("/now")
    public ResponseEntity<Map<String, Object>> sendNow() {
        List<UUID> devices = scheduler.resolveDeviceIds();
        if (devices.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No devices found."));
        }
        scheduler.generateAndSend();
        return ResponseEntity.ok(Map.of("sent", devices.size()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        List<UUID> devices = scheduler.resolveDeviceIds();
        return ResponseEntity.ok(Map.of(
                "devices", devices,
                "deviceCount", devices.size()
        ));
    }
}
