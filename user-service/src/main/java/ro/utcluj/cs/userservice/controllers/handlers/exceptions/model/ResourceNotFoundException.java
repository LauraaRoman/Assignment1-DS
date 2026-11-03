package ro.utcluj.cs.userservice.controllers.handlers.exceptions.model;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
