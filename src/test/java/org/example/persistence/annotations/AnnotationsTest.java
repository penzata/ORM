package org.example.persistence.annotations;

import org.example.persistence.utilities.AnnotationUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AnnotationsTest {

    @Test
    void WhenClassIsMarkedWithEntityAnnotationThenReturnTrue() {
        boolean result = AnnotationUtils.entityAnnotationIsPresent(WithAnno.class);

        assertTrue(result);
    }

    @Test
    void WhenClassIsNotMarkedWithEntityAnnotationThenReturnFalse() {
        boolean result = AnnotationUtils.entityAnnotationIsPresent(WithoutAnno.class);

        assertFalse(result);
    }

    @Test
    void WhenTableAnnotationIsAbsentThenReturnClassNameInPlural() {
        String expectedTableName = "WithoutAnnos";

        String tableName = AnnotationUtils.getTableName(WithoutAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenTableAnnotationIsPresentAndNameIsNotDefaultThenReturnNameFromAnnotation() {
        String expectedTableName = "named_table";

        String tableName = AnnotationUtils.getTableName(WithAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenTableAnnotationIsPresentAndNameIsDefaultThenReturnClassNameInPlural() {
        String expectedTableName = "DefaultWithAnnos";

        String tableName = AnnotationUtils.getTableName(DefaultWithAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Entity
    @Table(name = "named_table")
    static class WithAnno {
        Long trialId;
        String trialFirstName;
    }

    @Entity
    @Table
    static class DefaultWithAnno {
        Long trialId;
        String trialFirstName;
    }

    static class WithoutAnno {
    }
}
