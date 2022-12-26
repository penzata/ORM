package org.example.persistence.annotations;

import org.example.persistence.utilities.AnnotationUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
        String expectedTableName = "DefaultAnnos";

        String tableName = AnnotationUtils.getTableName(DefaultAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenColumnAnnotationIsAbsentThenReturnFieldName() {
        String expectedFieldName = "trialId";

        Field[] declaredFields = WithoutAnno.class.getDeclaredFields();
        String fieldName = AnnotationUtils.getFieldName(declaredFields[0]);

        assertEquals(expectedFieldName, fieldName);
    }

    @Test
    void WhenColumnAnnotationIsPresentAndNameIsNotDefaultThenReturnNameFromAnnotation() {
        String expectedFieldName = "id";

        Field[] declaredFields = WithAnno.class.getDeclaredFields();
        String fieldName = AnnotationUtils.getFieldName(declaredFields[0]);

        assertEquals(expectedFieldName, fieldName);
    }

    @Test
    void WhenColumnAnnotationIsPresentAndNameIsDefaultThenReturnFieldName() {
        String expectedTableName = "trialId";

        Field[] declaredFields = DefaultAnno.class.getDeclaredFields();
        String fieldName = AnnotationUtils.getFieldName(declaredFields[0]);

        assertEquals(expectedTableName, fieldName);
    }

    @Test
    void WhenUniqueValueInColumnIsNotPresentThenReturnDefaultOne() throws NoSuchFieldException {
        boolean expectedUniqueValue = false;

        Field trialId = DefaultAnno.class.getDeclaredField("trialId");
        boolean uniqueDefault = AnnotationUtils.isUnique(trialId);
        Field trialId1 = WithAnno.class.getDeclaredField("trialId");
        boolean uniqueSet = AnnotationUtils.canBeNull(trialId1);

        assertEquals(expectedUniqueValue, uniqueDefault);
        assertNotEquals(expectedUniqueValue, uniqueSet);
    }

    @Test
    void WhenNullableValueInColumnIsNotPresentThenReturnDefaultOne() throws NoSuchFieldException {
        boolean expectedNullableValue = true;

        Field trialFirstName = DefaultAnno.class.getDeclaredField("trialFirstName");
        boolean nullableDefault = AnnotationUtils.canBeNull(trialFirstName);
        Field trialFirstName1 = WithAnno.class.getDeclaredField("trialFirstName");
        boolean nullableSet = AnnotationUtils.canBeNull(trialFirstName1);

        assertEquals(expectedNullableValue, nullableDefault);
        assertNotEquals(expectedNullableValue, nullableSet);
    }

    @Entity
    @Table(name = "named_table")
    static class WithAnno {
        @Column(name = "id", unique = true)
        Long trialId;
        @Column(name = "first_name", nullable = false)
        String trialFirstName;
    }

    @Entity
    @Table
    static class DefaultAnno {
        @Column
        Long trialId;
        @Column
        String trialFirstName;
    }

    static class WithoutAnno {
        Long trialId;
        String trialFirstName;
    }
}