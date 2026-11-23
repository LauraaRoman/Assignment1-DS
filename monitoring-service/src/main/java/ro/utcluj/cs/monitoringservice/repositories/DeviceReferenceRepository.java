package ro.utcluj.cs.monitoringservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcluj.cs.monitoringservice.entities.DeviceReference;

import java.util.List;
import java.util.UUID;

public interface DeviceReferenceRepository extends JpaRepository<DeviceReference, UUID> {
    List<DeviceReference> findByUserId(UUID userId);
}
