package ro.utcluj.cs.userservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import ro.utcluj.cs.userservice.dtos.UserDTO;
import ro.utcluj.cs.userservice.dtos.UserInsertDTO;
import ro.utcluj.cs.userservice.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> dtos = userService.findUsers();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<UUID> insertUser(@Valid @RequestBody UserInsertDTO userDTO) {
        UUID userID = userService.insert(userDTO);

        return new ResponseEntity<>(userID, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UserInsertDTO userDTO) {

        UserDTO updatedUser = userService.update(id, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        userService.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
