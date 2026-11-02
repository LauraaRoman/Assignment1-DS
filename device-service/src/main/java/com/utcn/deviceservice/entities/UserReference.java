package com.utcn.deviceservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user_references")
public class UserReference {
    @Id
    private UUID id;

    public UserReference() {}
    public UserReference(UUID id) { this.id = id; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}