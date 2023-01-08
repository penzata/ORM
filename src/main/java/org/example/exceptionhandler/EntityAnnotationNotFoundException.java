package org.example.exceptionhandler;

public class EntityAnnotationNotFoundException extends RuntimeException {

    public EntityAnnotationNotFoundException(Class<?> clss) {
        super("\nThe annotation @Entity is absent from the entity class. " +
                "\nThe annotation must be assign to the entity in order for ORM to work :" + clss);
    }
}
