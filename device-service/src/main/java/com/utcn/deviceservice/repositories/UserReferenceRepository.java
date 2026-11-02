package com.utcn.deviceservice.repositories;

import com.utcn.deviceservice.entities.UserReference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserReferenceRepository extends JpaRepository<UserReference, UUID> {}