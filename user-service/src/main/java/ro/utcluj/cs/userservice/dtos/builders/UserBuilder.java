package ro.utcluj.cs.userservice.dtos.builders;

import ro.utcluj.cs.userservice.dtos.UserInsertDTO;
import ro.utcluj.cs.userservice.dtos.UserDTO;
import ro.utcluj.cs.userservice.entities.User;

public class UserBuilder {
    private UserBuilder() {}

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }

    public static User toEntity(UserInsertDTO userInsertDTO) {
        return new User(
                userInsertDTO.getUsername(),
                userInsertDTO.getPassword(),
                userInsertDTO.getRole()
        );
    }
}
