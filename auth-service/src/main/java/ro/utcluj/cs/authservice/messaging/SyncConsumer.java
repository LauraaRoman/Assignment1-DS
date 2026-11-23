package ro.utcluj.cs.authservice.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.utcluj.cs.authservice.entities.UserCredential;
import ro.utcluj.cs.authservice.repositories.CredentialRepository;

import java.util.Map;

@Component
public class SyncConsumer {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public SyncConsumer(CredentialRepository credentialRepository,
                        PasswordEncoder passwordEncoder,
                        ObjectMapper objectMapper) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}")
    public void handleSyncMessage(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<>() {});
            String type = (String) payload.get("type");
            if (!"USER".equals(type)) return;

            String username = (String) payload.get("username");
            String password = (String) payload.get("password");
            String role = (String) payload.get("role");

            if (credentialRepository.findByUsername(username).isPresent()) return;

            UserCredential credential = new UserCredential();
            credential.setUsername(username);
            credential.setPassword(passwordEncoder.encode(password));
            credential.setRole(role != null ? role.toUpperCase() : "CLIENT");
            credentialRepository.save(credential);
        } catch (Exception e) {
            // malformed message — skip
        }
    }
}
