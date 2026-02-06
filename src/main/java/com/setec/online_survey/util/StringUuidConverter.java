package com.setec.online_survey.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;
import java.nio.ByteBuffer;

@Converter(autoApply = false)
public class StringUuidConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(attribute);
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            return bb.array();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length != 16) return null;
        ByteBuffer bb = ByteBuffer.wrap(dbData);
        return new UUID(bb.getLong(), bb.getLong()).toString();
    }
}