/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

/**
 * JPA attribute converter for {@link MonthDay}.
 * <p>
 * This converter persists {@link MonthDay} values as ISO-8601 compatible
 * strings in the format {@code --MM-dd} and converts them back when reading
 * from the database.
 * <p>
 * It is automatically applied to all {@link MonthDay} entity attributes.
 */
@Converter(autoApply = true)
public class MonthDayConverter implements AttributeConverter<MonthDay, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("--MM-dd");

    /**
     * Converts a {@link MonthDay} attribute to its database column representation.
     *
     * @param attribute the {@link MonthDay} value from the entity
     * @return formatted string representation or {@code null} if the attribute is null
     */
    @Override
    public String convertToDatabaseColumn(MonthDay attribute) {
        return attribute != null ? attribute.format(FORMATTER) : null;
    }

    /**
     * Converts a database column value into a {@link MonthDay} entity attribute.
     *
     * @param dbData the stored string representation
     * @return parsed {@link MonthDay} or {@code null} if the database value is null
     */
    @Override
    public MonthDay convertToEntityAttribute(String dbData) {
        return dbData != null ? MonthDay.parse(dbData, FORMATTER) : null;
    }
}
