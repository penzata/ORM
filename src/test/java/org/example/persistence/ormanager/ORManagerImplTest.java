package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Table;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ORManagerImplTest {
    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement st;
    Table createdDudesTable;
    Dude dude1;

    @AfterEach
    void tearDown() throws SQLException {
        st = connection.prepareStatement("DROP TABLE dudes");
        st.executeUpdate();

        if (connection != null) {
            connection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (st != null) {
            st.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Dude.class);
        connection = dataSource.getConnection();
        createdDudesTable = new Table(dataSource, "dudes");
        dude1 = new Dude("Bob");
    }

    @Test
    void CanSaveOneDudeToDatabaseAndReturnDudeWithId() {
        Dude savedDude = manager.save(dude1);

        assertThat(savedDude.getId()).isNotNull();

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Test
    void CanSaveTwoDudesToDatabaseAndReturnDudesWithId() {
        Dude savedDude = manager.save(dude1);
        Dude savedSecondDude = manager.save(new Dude("Dale"));

        assertThat(savedDude.getId()).isPositive();
        assertThat(savedSecondDude.getId()).isGreaterThan(savedDude.getId());

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Test
    void WhenSavingExistingObjectIntoDatabaseThenReturnTheSameAndDontSaveIt() {
        Dude st = new Dude("Shelly");
        manager.save(st);
        manager.save(st);
        manager.save(st);

        assertThat(createdDudesTable).hasNumberOfRows(1);

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Test
    void canFindPersonById() {
        Dude savedDude = manager.save(new Dude("Harry"));

        Optional<Dude> foundDude = manager.findById(savedDude.getId(), Dude.class);

        assertThat(foundDude).contains(savedDude);
    }

    @Test
    void WhenIdDoesntExistsThenReturnEmptyOptional() {
        Optional<Dude> personToBeFound = manager.findById(-1L, Dude.class);

        assertThat(personToBeFound).isNotPresent();
    }

    @Test
    void WhenFindAllThenReturnAllSavedToDBObjects() {
        manager.save(new Dude("Ivan"));
        manager.save(new Dude("Petkan"));

        List<Dude> allDudes = manager.findAll(Dude.class);

        assertThat(allDudes).hasSize(2);
        assertThat(createdDudesTable).row(1)
                .value().isEqualTo(2)
                .value().isEqualTo("Petkan");

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Test
    void WhenRegisterAnEntityReturnATableMatchingItsFields() {
        @Entity
        @org.example.persistence.annotations.Table(name = "trial_table")
        class TrialTable {
            @Id
            @Column(name = "trial_id")
            int trialId;
            @Column(name = "trial_first_name", nullable = false)
            String trialFirstName;
            @Column(nullable = false)
            boolean under18;
        }
        Table table = new Table(dataSource, "trial_table");

        manager.register(TrialTable.class);

        assertThat(table).hasNumberOfColumns(3);
        assertThat(table).column(1)
                .hasColumnName("trial_first_name");

        output(table).toFile("tableFromTest");
    }

    @Test
    void WhenSavingThreeEntitiesTwoDBThenReturnRecordsCountToBeEqualToThree() {
        long startCount = manager.recordsCount(Dude.class);
        Dude un = manager.save(new Dude("Un"));
        Dude dos = manager.save(new Dude("Dos"));
        Dude tres = manager.save(new Dude("Tres"));

        long endCount = manager.recordsCount(Dude.class);

        assertThat(endCount).isEqualTo(startCount + 3);
    }

    @Test
    void WhenDeletingFromRecordsThenReturnRecordsCountWithOneRecordLess() {
        Dude savedDude = manager.save(new Dude("Laura"));
        long startCount = manager.recordsCount(Dude.class);

        manager.delete(savedDude);
        long endCount = manager.recordsCount(Dude.class);

        assertThat(endCount).isEqualTo(startCount - 1);
    }

    @Test
    void WhenDeletingRecordThenReturnTrue() {

        manager.save(dude1);

        boolean result = manager.delete(dude1);

        assertTrue(result);
    }

    @Test
    void WhenDeletingRecordThatDoesntExistsThenReturnFalse() {
        Dude notSavedInDBDude = new Dude("Andi");

        boolean result = manager.delete(notSavedInDBDude);

        assertFalse(result);
    }

    @Test
    void WhenDeletingRecordSetAutoGeneratedIdToNull() {
        Dude savedDude = manager.save(new Dude("Bobby"));

        manager.delete(savedDude);

        assertThat(savedDude.getId()).isNull();
    }

    @Test
    void canDeleteMultipleRecords() {
        Dude catherine = manager.save(new Dude("Catherine"));
        Dude audrey = manager.save(new Dude("Audrey"));
        long startCount = manager.recordsCount(Dude.class);

        manager.delete(catherine, audrey);
        long endCount = manager.recordsCount(Dude.class);

        assertThat(endCount).isLessThanOrEqualTo(startCount - 2);
    }

    @Test
    void WhenUpdatingRecordAndFindItByIdThenReturnTheUpdatedRecord() {
        Dude savedDude = manager.save(new Dude("Donna"));
        Dude foundDude = manager.findById(savedDude.getId(), Dude.class).get();

        foundDude.setFirstName("Don");
        manager.update(foundDude);
        Dude foundUpdatedDude = manager.findById(foundDude.getId(), Dude.class).get();

        assertThat(foundUpdatedDude.getFirstName()).isEqualTo(foundDude.getFirstName());
        assertThat(foundUpdatedDude).usingRecursiveComparison().isEqualTo(foundDude);

        assertThat(createdDudesTable).column(0)
                .value().isEqualTo(foundDude.getId())
                .column(1)
                .value().isEqualTo("Don");

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Test
    void canUpdateRecord() {
        Dude savedDude = manager.save(new Dude("Donna"));
        Dude returnedDude = manager.findById(savedDude.getId(), Dude.class).get();

        savedDude.setFirstName("Dina");
        manager.update(savedDude);
        Dude foundDude = manager.findById(savedDude.getId(), Dude.class).get();

        assertThat(foundDude.getFirstName()).isNotEqualTo(returnedDude.getFirstName());
        assertThat(foundDude).usingRecursiveComparison().isNotEqualTo(returnedDude);

        assertThat(createdDudesTable).column(0)
                .value().isEqualTo(savedDude.getId())
                .column(1)
                .value().isEqualTo("Dina");

        output(createdDudesTable).toFile("tableFromTest");
    }

    @Data
    @Entity
    static class Dude {
        @Id
        Long id;
        String firstName;
        String secondName;
        Integer age;
        LocalDate graduateAcademy;
        public Dude(String firstName) {
            this(firstName, "", 36, LocalDate.now());
        }

        public Dude(String firstName, String secondName, Integer age, LocalDate graduateAcademy) {
            this.firstName = firstName;
            this.secondName = secondName;
            this.age = age;
            this.graduateAcademy = graduateAcademy;
        }

        private Dude() {
        }
    }

}