package ro.utcluj.cs.simulator;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class SimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Bean
    public ApplicationRunner autoBackfill(SimulatorScheduler scheduler) {
        return args -> {
            String date = args.getOptionValues("date") != null
                    ? args.getOptionValues("date").get(0)
                    : null;

            if (date != null) {
                LocalDate target = LocalDate.parse(date);
                scheduler.backfillDay(target);
                System.exit(0);
            }
        };
    }
}
