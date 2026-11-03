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
import ro.utcluj.cs.userservice.repositories.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${device.service.url:http://device-service:8081}")
    private String deviceServiceBaseUrl;

    @Value("${auth.service.url:http://auth-service:8082}")
    private String authServiceBaseUrl;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
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
        // Save first so Hibernate generates the UUID
        User user = userRepository.save(UserBuilder.toEntity(userDTO));

        // Sync the new user's ID to device-service
        String syncUrl = deviceServiceBaseUrl + "/devices/sync-user";
        try {
            restTemplate.postForEntity(syncUrl, user.getId(), Void.class);
        } catch (Exception e) {
            // Rollback: delete the just-created user if sync fails
            userRepository.deleteById(user.getId());
            throw new RuntimeException("Could not sync user with Device Service. Creation cancelled.");
        }

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
            String deviceServiceUrl = deviceServiceBaseUrl + "/devices/user/" + id;
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
