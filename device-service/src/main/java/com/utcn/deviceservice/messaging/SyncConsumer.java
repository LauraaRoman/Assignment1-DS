package com.utcn.deviceservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utcn.deviceservice.entities.UserReference;
import com.utcn.deviceservice.repositories.UserReferenceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SyncConsumer {

    private final UserReferenceRepository userReferenceRepository;
    private final ObjectMapper objectMapper;

    public SyncConsumer(UserReferenceRepository userReferenceRepository, ObjectMapper objectMapper) {
        this.userReferenceRepository = userReferenceRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}")
    public void handleSync(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String type = (String) payload.get("type");

            if ("USER".equals(type)) {
                UUID userId = UUID.fromString((String) payload.get("id"));
                if (!userReferenceRepository.existsById(userId)) {
                    userReferenceRepository.save(new UserReference(userId));
                }
            }
        } catch (Exception e) {
        }
    }
}
