package ro.utcluj.cs.userservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UUID insert(UserInsertDTO userDTO) {
        User userEntity = UserBuilder.toEntity(userDTO);

        userEntity = userRepository.save(userEntity);

        return userEntity.getId();
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

        userRepository.deleteById(id);
    }
}
