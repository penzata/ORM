package org.example.persistence.ormanager;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The ORM manager works only with autogenerated at DB side Long or Integer ID's.
 * Every entity class must have an @Entity annotation.
 * The ID field of the entity class should always be placed as the first field and must have an @Id annotation.
 */
public interface ORManager {

    /**
     * Providing an entity class or multiple ones, creates table(s) in DB based on the entity's properties.
     *
     * @param entityClasses Single or array of classes.
     */
    void register(Class... entityClasses);

    /**
     * Persists an entity. It assigns an identifier if entity doesn't exist in the DB.
     * If ID is present, save method performs an update.
     * In both cases the method returns the saved/updated entity.
     *
     * @param o Generic object.
     * @return Saved Object
     */
    <T> T save(T o);

    /**
     * It is intended for a first save of a new entity to DB.
     * It assigns an identifier if entity doesn't exist in the DB.
     *
     * @param o Object.
     */
    void persist(Object o);

    /**
     * @param id  Serializable, Long or Integer ID number.
     * @param cls Generic Class.
     * @return the current object, based on provided ID, if exists or empty optional,
     * from the correct table, based on the provided class.
     */
    <T> Optional<T> findById(Serializable id, Class<T> cls);

    /**
     * @param cls Class.
     * @return a collection of all the objects, from the table, based on the provided class;
     */
    <T> List<T> findAll(Class<T> cls);

    /**
     * Updates the existing object, and updates its row in the DB table.
     * If the object's identifier does not exist, it throws an exception.
     *
     * @param o Generic object.
     * @return the updated object.
     */
    <T> T update(T o);

    /**
     * Synchronizing the provided object with its corresponding row in the DB table
     * and re-populate the object with the latest data available in database.
     *
     * @param o Generic object.
     * @return the updated object with the latest data available in database.
     */
    <T> T refresh(T o);

    /**
     * Sets the autogenerated ID of the object to null if it was deleted from the DB side.
     *
     * @param o Object.
     * @return true or false if the provided object is successfully deleted from the DB.
     * If the object is not present in the DB, the method returns false.
     */
    boolean delete(Object o);

    /**
     * @param clss Class
     * @return the number of all records from the table, based on the provided class.
     */
    public long recordsCount(Class<?> clss);

    /**
     * Can delete multiple objects.
     *
     * @param objects An array of objects.
     */
    void delete(Object... objects);
}