package com.utcn.deviceservice.controllers.handlers.exceptions.model;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
