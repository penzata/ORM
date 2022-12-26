package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SerializationUtil {
    private static final List<Object> serializedData = new ArrayList<>();

    private SerializationUtil() {
    }

    // deserialize to Object from given file
    public static List<Object> deserialize(String fileName) {
        List<Object> deserializedData = new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            deserializedData = (List<Object>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        return deserializedData;
    }

    // serialize the given object and save it to file
    public static void serialize(Object obj, String fileName) {
        serializedData.add(obj);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(serializedData);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
