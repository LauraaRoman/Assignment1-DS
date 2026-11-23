package ro.utcluj.cs.userservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ro.utcluj.cs.userservice.controllers.handlers.exceptions.model.ResourceNotFoundException;
import ro.utcluj.cs.userservice.dtos.UserDTO;
import ro.utcluj.cs.userservice.dtos.UserInsertDTO;
import ro.utcluj.cs.userservice.dtos.builders.UserBuilder;
import ro.utcluj.cs.userservice.entities.User;
import ro.utcluj.cs.userservice.messaging.SyncPublisher;
import ro.utcluj.cs.userservice.repositories.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${auth.service.url:http://auth-service:8082}")
    private String authServiceBaseUrl;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final SyncPublisher syncPublisher;

    @Autowired
    public UserService(UserRepository userRepository, RestTemplate restTemplate, SyncPublisher syncPublisher) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.syncPublisher = syncPublisher;
    }

    public UserDTO findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        return UserBuilder.toUserDTO(user);
    }

    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with username: " + username));
        return UserBuilder.toUserDTO(user);
    }

    public List<UserDTO> findUsers() {
        return userRepository.findAll().stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO insert(UserInsertDTO userDTO) {
        UUID generatedId = UUID.randomUUID();

        User entity = UserBuilder.toEntity(userDTO);
        entity.setId(generatedId);
        User user = userRepository.save(entity);

        syncPublisher.publishUserSync(generatedId, userDTO.getUsername(), userDTO.getPassword(), userDTO.getRole());

        return UserBuilder.toUserDTO(user);
    }

    public UserDTO update(UUID id, UserInsertDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setPassword(userDTO.getPassword());
        existingUser.setRole(userDTO.getRole());

        existingUser = userRepository.save(existingUser);
        return UserBuilder.toUserDTO(existingUser);
    }

    public void delete(UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        try {
            String deviceServiceUrl = "http://device-service:8081" + "/devices/user/" + id;
            restTemplate.delete(deviceServiceUrl);
        } catch (Exception e) {
            System.err.println("Could not delete devices for user " + id);
        }

        try {
            String authServiceUrl = authServiceBaseUrl + "/auth/credentials/" + existingUser.getUsername();
            restTemplate.delete(authServiceUrl);
        } catch (Exception e) {
            System.err.println("Could not delete credentials for user " + existingUser.getUsername());
        }

        userRepository.deleteById(id);
    }
}
