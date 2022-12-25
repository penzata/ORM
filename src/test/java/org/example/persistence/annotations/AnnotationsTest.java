package org.example.persistence.annotations;

import org.example.persistence.utilities.AnnotationUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AnnotationsTest {

    @Test
    void WhenClassMarkedWithEntityAnnotationThenReturnTrue() {
        boolean result = AnnotationUtils.entityAnnotationIsPresent(TrialClassWithAnnotations.class);

        assertTrue(result);
    }

    @Test
    void WhenClassIsNotMarkedWithEntityAnnotationThenReturnFalse() {
        boolean result = AnnotationUtils.entityAnnotationIsPresent(TrialClassWithoutAnnotations.class);

        assertFalse(result);
    }

    @Entity
    static class TrialClassWithAnnotations {
        Long trialId;
        String trialFirstName;
    }

    static class TrialClassWithoutAnnotations {
    }
}
