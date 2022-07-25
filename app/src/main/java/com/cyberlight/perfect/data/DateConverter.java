package com.cyberlight.perfect.data;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @TypeConverter
    public LocalDate stringToLocalDate(String string) {
        return string != null ? LocalDate.parse(string, formatter) : null;
    }

    @TypeConverter
    public String localDateToString(LocalDate date) {
        return date != null ? date.format(formatter) : null;
    }
}