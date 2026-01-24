package com.oss.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDate locDate) {
        // Convert LocalDate to epoch milliseconds at start of day in system timezone
        return (locDate == null ? null :
            locDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Override
    public LocalDate convertToEntityAttribute(Long epochMillis) {
        // Convert epoch milliseconds to LocalDate
        if (epochMillis == null) {
            return null;
        }

        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }
}

