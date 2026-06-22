package be.cytomine.common.repository.utils;

// Duplicate from https://github.com/cytomine/cytomine/blob/d1404a97e5669051c26bdd6a0904bd2e9dafa38a/core/src/main/java/be/cytomine/utils/LongArrayToBytesConverter.java

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LongArrayToBytesConverter implements AttributeConverter<Long[], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(Long[] attribute) {
        if (attribute == null) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(attribute);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("cannot serialize", e);
        }
    }

    @Override
    public Long[] convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        try (ByteArrayInputStream b = new ByteArrayInputStream(dbData)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                Long[] data = (Long[]) o.readObject();
                return data;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot deserialize", e);
        }

    }
}
