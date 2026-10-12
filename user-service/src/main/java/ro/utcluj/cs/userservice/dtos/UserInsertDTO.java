package ro.utcluj.cs.userservice.dtos;

import jakarta.validation.constraints.NotNull;
import ro.utcluj.cs.userservice.dtos.validators.ValidRole;

public class UserInsertDTO {

    @NotNull(message = "Username cannot be null")
    private String username;

    @NotNull(message = "Password cannot be null")
    private String password;

    @ValidRole
    private String role;

    public UserInsertDTO() {
    }

    public UserInsertDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
