package ro.utcluj.cs.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcluj.cs.userservice.entities.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
