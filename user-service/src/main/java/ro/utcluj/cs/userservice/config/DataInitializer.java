package ro.utcluj.cs.userservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ro.utcluj.cs.userservice.entities.User;
import ro.utcluj.cs.userservice.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin123", "ADMIN");
            // Nu setăm ID manual — lăsăm Hibernate să genereze UUID-ul
            userRepository.save(admin);
            System.out.println("[DataInitializer] Admin user seeded: username=admin");
        }
    }
}
