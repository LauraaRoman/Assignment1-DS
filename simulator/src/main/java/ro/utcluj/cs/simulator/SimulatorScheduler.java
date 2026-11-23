package ro.utcluj.cs.simulator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ro.utcluj.cs.simulator.model.DeviceMeasurement;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SimulatorScheduler {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${rabbitmq.queue.data}")
    private String dataQueue;

    @Value("${rabbitmq.exchange.data}")
    private String dataExchange;

    @Value("${device.service.url:http://localhost:8081}")
    private String deviceServiceUrl;

    @Value("${simulator.device-ids:}")
    private String configuredDeviceIds;

    public SimulatorScheduler(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public List<UUID> resolveDeviceIds() {
        if (configuredDeviceIds != null && !configuredDeviceIds.isBlank()) {
            return Arrays.stream(configuredDeviceIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
        return fetchDeviceIds();
    }

    @Scheduled(fixedRateString = "${simulator.interval-ms:600000}")
    public void generateAndSend() {
        List<UUID> ids = resolveDeviceIds();
        if (ids.isEmpty()) return;
        sendNow(Instant.now(), ids);
    }

    public void backfillDay(LocalDate date) {
        List<UUID> ids = resolveDeviceIds();
        if (ids.isEmpty()) return;

        for (int hour = 0; hour < 24; hour++) {
            for (int slot = 0; slot < 6; slot++) {
                LocalDateTime ldt = date.atTime(hour, slot * 10);
                Instant ts = ldt.toInstant(ZoneOffset.UTC);
                sendNow(ts, ids);
            }
        }
    }

    public List<UUID> fetchDeviceIds() {
        try {
            String json = restTemplate.getForObject(deviceServiceUrl + "/devices", String.class);
            List<Map<String, Object>> devices = objectMapper.readValue(json, new TypeReference<>() {});
            List<UUID> ids = new ArrayList<>();
            for (Map<String, Object> d : devices) {
                Object idObj = d.get("id");
                if (idObj != null) ids.add(UUID.fromString(idObj.toString()));
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }

    private void sendNow(Instant timestamp, List<UUID> deviceIds) {
        int hour = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC).getHour();
        for (UUID deviceId : deviceIds) {
            double value = computeValue(hour);
            DeviceMeasurement m = new DeviceMeasurement(timestamp.toEpochMilli(), deviceId, value);
            publish(m);
        }
    }

    private double computeValue(int hour) {
        double base = 0.05 + random.nextDouble() * 0.25;
        double factor = hourFactor(hour);
        double noise = (random.nextDouble() - 0.5) * 0.05;
        return Math.max(0.001, Math.round((base * factor + noise) * 10000.0) / 10000.0);
    }

    private double hourFactor(int hour) {
        if (hour < 6)  return 0.30 + random.nextDouble() * 0.10;
        if (hour < 9)  return 0.60 + random.nextDouble() * 0.20;
        if (hour < 17) return 0.70 + random.nextDouble() * 0.20;
        if (hour < 22) return 0.90 + random.nextDouble() * 0.20;
        return 0.40 + random.nextDouble() * 0.10;
    }

    private void publish(DeviceMeasurement m) {
        try {
            String json = objectMapper.writeValueAsString(m);
            rabbitTemplate.convertAndSend(dataExchange, dataQueue, json);
        } catch (JsonProcessingException e) {
        }
    }
}
