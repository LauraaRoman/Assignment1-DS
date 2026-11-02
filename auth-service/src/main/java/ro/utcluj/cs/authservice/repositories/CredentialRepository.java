package ro.utcluj.cs.authservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcluj.cs.authservice.entities.UserCredential;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<UserCredential, UUID> {
    Optional<UserCredential> findByUsername(String username);
}