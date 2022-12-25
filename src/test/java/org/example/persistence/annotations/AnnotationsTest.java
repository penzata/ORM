package org.example.persistence.annotations;

import org.example.persistence.utilities.AnnotationUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AnnotationsTest {

    @Entity
    static class TrialClass {
        Long trialId;
        String trialFirstName;
    }

    @Test
    void WhenClassMarkedWithEntityAnnotationThenReturnTrue() {
        boolean result = AnnotationUtils.entityAnnotationIsPresent(TrialClass.class);

        assertTrue(result);

    }
}
