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

@Converter(autoApply = true)
public class MonthDayConverter implements AttributeConverter<MonthDay, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("--MM-dd");

    @Override
    public String convertToDatabaseColumn(MonthDay attribute) {
        return attribute != null ? attribute.format(FORMATTER) : null;
    }

    @Override
    public MonthDay convertToEntityAttribute(String dbData) {
        return dbData != null ? MonthDay.parse(dbData, FORMATTER) : null;
    }
}
