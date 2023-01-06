package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.example.exceptionhandler.ExceptionHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SerializationUtil {
    private static final List<Object> serializedData = new ArrayList<>();
    private static String fullFileName;
    private static final String SUFFIX = "_serialization.ser";

    private SerializationUtil() {
    }

    public static <T> void serialize(T obj, String simpleFileName) {
        fullFileName = simpleFileName + SUFFIX;
        serializeIntoList(obj, fullFileName);
    }

    // serialize the given object and save it to file
    private static <T> void serializeIntoList(T obj, String fullFileName) {
        serializedData.add(obj);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fullFileName))) {
            out.writeObject(serializedData);
        } catch (IOException e) {
            ExceptionHandler.inputOutput(e);
        }
    }

    public static <T> List<T> deserialize(String simpleFileName) {
        return simpleFileName.equals(fullFileName) ?
                deserializeList(simpleFileName) : deserializeList(simpleFileName + SUFFIX);
    }

    // deserialize to Objects from given file
    private static <T> List<T> deserializeList(String fileName) {
        List<T> deserializedData = new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            deserializedData = (List<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        return deserializedData;
    }
}
