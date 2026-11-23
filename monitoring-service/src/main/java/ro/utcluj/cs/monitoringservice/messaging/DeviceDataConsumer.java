package ro.utcluj.cs.monitoringservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ro.utcluj.cs.monitoringservice.dtos.DeviceMeasurementDTO;
import ro.utcluj.cs.monitoringservice.services.MonitoringService;

@Component
public class DeviceDataConsumer {

    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    public DeviceDataConsumer(MonitoringService monitoringService, ObjectMapper objectMapper) {
        this.monitoringService = monitoringService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.data}", containerFactory = "dataListenerContainerFactory")
    public void handleMeasurement(String message) {
        try {
            DeviceMeasurementDTO measurement = objectMapper.readValue(message, DeviceMeasurementDTO.class);
            monitoringService.processMeasurement(measurement);
        } catch (Exception e) {
            // malformed message — skip
        }
    }
}
