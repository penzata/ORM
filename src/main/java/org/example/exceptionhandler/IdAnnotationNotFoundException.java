package org.example.exceptionhandler;

public class IdAnnotationNotFoundException extends RuntimeException {

    public IdAnnotationNotFoundException(Class<?> clss) {
        super("\nThe annotation @Id is absent from the entity class. " +
                "\nNeeds to be assigned to Long or Integer field: " + clss);
    }
}
