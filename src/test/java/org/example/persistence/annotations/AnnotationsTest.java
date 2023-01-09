package org.example.persistence.annotations;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.example.persistence.utilities.AnnotationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class AnnotationsTest {

    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement pstmt;

    @AfterEach
    void tearDown() throws SQLException {
        pstmt = connection.prepareStatement("DROP TABLE IF EXISTS students");
        pstmt.executeUpdate();
        pstmt = connection.prepareStatement("DROP TABLE IF EXISTS academies");
        pstmt.executeUpdate();

        if (connection != null) {
            connection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (pstmt != null) {
            pstmt.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        connection = dataSource.getConnection();
    }

    @Test
    void WhenClassIsMarkedWithEntityAnnotationThenReturnTrue() {
        boolean result = entityAnnotationIsPresent(WithAnno.class);

        assertTrue(result);
    }

    @Test
    void WhenClassIsNotMarkedWithEntityAnnotationThenReturnFalse() {
        boolean result = entityAnnotationIsPresent(WithoutAnno.class);

        assertFalse(result);
    }

    @Test
    void WhenFieldIsMarkedWithIdAnnotationThenReturnTrue() {
        boolean result = idAnnotationIsPresent(WithAnno.class);

        assertTrue(result);
    }

    @Test
    void WhenFieldIsNotMarkedWithIdAnnotationThenReturnFalse() {
        boolean result = idAnnotationIsPresent(WithoutAnno.class);

        assertFalse(result);
    }

    @Test
    void WhenTableAnnotationIsAbsentThenReturnClassNameInPlural() {
        String expectedTableName = "withoutannos";

        String tableName = getTableName(WithoutAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenTableAnnotationIsPresentAndNameIsNotDefaultThenReturnNameFromAnnotation() {
        String expectedTableName = "named_table";

        String tableName = getTableName(WithAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenTableAnnotationIsPresentAndNameIsDefaultThenReturnClassNameInPlural() {
        String expectedTableName = "defaultannos";

        String tableName = getTableName(DefaultAnno.class);

        assertEquals(expectedTableName, tableName);
    }

    @Test
    void WhenColumnAnnotationIsAbsentThenReturnFieldName() {
        String expectedFieldName = "trialId";

        Field[] declaredFields = WithoutAnno.class.getDeclaredFields();
        String fieldName = getColumnName(declaredFields[0]);

        assertEquals(expectedFieldName, fieldName);
    }

    @Test
    void WhenColumnAnnotationIsPresentAndNameIsNotDefaultThenReturnNameFromAnnotation() {
        String expectedFieldName = "id";

        Field[] declaredFields = WithAnno.class.getDeclaredFields();
        String fieldName = getColumnName(declaredFields[0]);

        assertEquals(expectedFieldName, fieldName);
    }

    @Test
    void WhenColumnAnnotationIsPresentAndNameIsDefaultThenReturnFieldName() {
        String expectedTableName = "trialAge";

        Field[] declaredFields = DefaultAnno.class.getDeclaredFields();
        String fieldName = getColumnName(declaredFields[0]);

        assertEquals(expectedTableName, fieldName);
    }

    @Test
    void WhenUniqueValueInColumnIsNotPresentThenReturnDefaultOne() throws NoSuchFieldException {
        boolean expectedUniqueValue = false;

        Field trialId = DefaultAnno.class.getDeclaredField("trialId");
        boolean uniqueDefault = isUnique(trialId);
        Field trialId1 = WithAnno.class.getDeclaredField("trialId");
        boolean uniqueSet = canBeNull(trialId1);

        assertEquals(expectedUniqueValue, uniqueDefault);
        assertNotEquals(expectedUniqueValue, uniqueSet);
    }

    @Test
    void WhenNullableValueInColumnIsNotPresentThenReturnDefaultOne() throws NoSuchFieldException {
        boolean expectedNullableValue = true;

        Field trialFirstName = DefaultAnno.class.getDeclaredField("trialFirstName");
        boolean nullableDefault = canBeNull(trialFirstName);
        Field trialFirstName1 = WithAnno.class.getDeclaredField("trialFirstName");
        boolean nullableSet = canBeNull(trialFirstName1);

        assertEquals(expectedNullableValue, nullableDefault);
        assertNotEquals(expectedNullableValue, nullableSet);
    }

    @Test
    void WhenSaveAndIdAnnotationIsAtRandomPlaceThenReturnCorrectObject() {
        DefaultAnno defaultAnno = new DefaultAnno(13, "Leatherface");

        manager.register(DefaultAnno.class);
        manager.save(defaultAnno);

        assertEquals(1, defaultAnno.getTrialId());
        dataSource.close();
    }

    @Entity
    @Table(name = "named_table")
    static class WithAnno {
        @Id
        @Column(name = "id", unique = true)
        Long trialId;
        @Column(name = "first_name", nullable = false)
        String trialFirstName;
    }

    @Data
    @Entity
    @Table
    static class DefaultAnno {
        @Column
        Integer trialAge;
        @Id
        @Column
        Long trialId;
        @Column
        String trialFirstName;

        public DefaultAnno(Integer trialAge, String trialFirstName) {
            this.trialAge = trialAge;
            this.trialFirstName = trialFirstName;
        }
    }

    static class WithoutAnno {
        Long trialId;
        String trialFirstName;
    }
}