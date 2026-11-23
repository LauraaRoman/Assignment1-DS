package com.utcn.deviceservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SyncPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    public SyncPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDeviceSync(UUID deviceId, UUID userId, double maxHourlyConsumption) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DEVICE");
        message.put("id", deviceId.toString());
        message.put("user_id", userId.toString());
        message.put("max_hourly_consumption", maxHourlyConsumption);
        sendMessage(message);
    }

    private void sendMessage(Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(syncExchange, "", json);
        } catch (JsonProcessingException e) {
        }
    }
}
