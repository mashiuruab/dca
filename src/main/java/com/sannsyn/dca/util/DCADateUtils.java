package com.sannsyn.dca.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * An utility class for date manipulation
 * <p>
 * Created by jobaer on 4/28/17.
 */
public class DCADateUtils {
    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate getDateFromString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    public static String makeDateString(LocalDate day) {
        if (day == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return day.format(formatter);
    }

    public static String makeDateString(Date day) {
        if(day == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(day);
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
