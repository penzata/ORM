package org.example.exceptionhandler;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Object o) {
        super("\nCould not find the current entity :" + o);
    }
}
