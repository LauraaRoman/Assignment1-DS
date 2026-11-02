package ro.utcluj.cs.authservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.utcluj.cs.authservice.entities.UserCredential;
import ro.utcluj.cs.authservice.repositories.CredentialRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (credentialRepository.findByUsername("admin").isEmpty()) {
            UserCredential admin = new UserCredential();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            credentialRepository.save(admin);
            System.out.println("[DataInitializer] Admin credentials seeded: username=admin, password=admin123");
        }
    }
}
