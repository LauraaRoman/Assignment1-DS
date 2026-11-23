package ro.utcluj.cs.monitoringservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ro.utcluj.cs.monitoringservice.dtos.SyncMessageDTO;
import ro.utcluj.cs.monitoringservice.services.MonitoringService;

@Component
public class SyncConsumer {

    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    public SyncConsumer(MonitoringService monitoringService, ObjectMapper objectMapper) {
        this.monitoringService = monitoringService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}", containerFactory = "syncListenerContainerFactory")
    public void handleSync(String message) {
        try {
            SyncMessageDTO sync = objectMapper.readValue(message, SyncMessageDTO.class);
            if ("DEVICE".equals(sync.getType())) {
                monitoringService.syncDevice(sync.getId(), sync.getUserId(), sync.getMaxHourlyConsumption());
            }
        } catch (Exception e) {
            // malformed message — skip
        }
    }
}
