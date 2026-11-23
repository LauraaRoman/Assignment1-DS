package ro.utcluj.cs.monitoringservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcluj.cs.monitoringservice.entities.HourlyConsumption;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {

    Optional<HourlyConsumption> findByDeviceIdAndConsumptionDateAndHour(UUID deviceId, LocalDate date, int hour);

    List<HourlyConsumption> findByDeviceIdAndConsumptionDate(UUID deviceId, LocalDate date);
}
