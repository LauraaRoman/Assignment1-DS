package ro.utcluj.cs.authservice.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.utcluj.cs.authservice.entities.UserCredential;
import ro.utcluj.cs.authservice.repositories.CredentialRepository;
import ro.utcluj.cs.authservice.security.JwtUtil;

@Service
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String register(UserCredential credential) {
        if ("ADMIN".equalsIgnoreCase(credential.getRole())) {
            throw new RuntimeException("Cannot register an ADMIN account.");
        }
        if (credentialRepository.findByUsername(credential.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }
        credential.setRole(credential.getRole() != null ? credential.getRole().toUpperCase() : "CLIENT");
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        credentialRepository.save(credential);
        return "User registered successfully";
    }

    public String login(String username, String password) {
        UserCredential user = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        return jwtUtil.generateToken(username, user.getRole());
    }

    public void deleteByUsername(String username) {
        UserCredential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Credential not found for username: " + username));
        credentialRepository.delete(credential);
    }
}
