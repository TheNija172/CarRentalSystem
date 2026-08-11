package org.example.carrentalsystem.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String message) {
        super(message);
    }
}
