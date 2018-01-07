package edu.gbcg.dbInteraction;

import edu.gbcg.utils.TSL;

import java.time.*;
import java.util.TimeZone;

/**
 * Class to convert to and from seconds from epoch UTC, Java LocalDateTime and SQLite datetime
 * objects
 */
public class TimeFormatter {
    /**
     * Convert a seconds from epoch UTC string to a LocalDateTime object
     * @param utcSeconds The seconds from epoch string
     * @return The LocalDateTime object
     */
    public static LocalDateTime utcSecondsToLDT(String utcSeconds){
        return utcSecondsToLDT(Long.parseLong(utcSeconds));
    }

    /**
     * Convert a utc timestamp to a LocalDateTime object using the default system timezone
     * @param utcSeconds
     * @return The LocalDateTime object
     */
    public static LocalDateTime utcSecondsToLDT(Long utcSeconds){
        Instant i = Instant.ofEpochSecond(utcSeconds);
        ZonedDateTime z = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId());
        return z.toLocalDateTime();
    }

    /**
     * Convert a LocalDateTime object to UTC seconds
     * @param ldt The local date time object
     * @return the UTC time as a long
     */
    public static long LDTtoUTCSeconds(LocalDateTime ldt){
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return zdt.withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond();
    }

    /**
     * Convert an SQLite compatible time string to UTC seconds
     * @param sqlTime The SQLite compatible time string
     * @return utc time as a long
     */
    public static long sqlTimeToUTC(String sqlTime){
        return SQLDateTimeToJavaDateTime(sqlTime).atZone(ZoneId.systemDefault()).toEpochSecond();
    }


    /**
     * Convert a java LocalDateTime object to an SQLite DateTime compatible object
     * @param ldt
     * @return The SQLite DateTime compatible time string
     */
    public static String javaDateTimeToSQLDateTime(LocalDateTime ldt){
        int year = ldt.getYear();
        int month = ldt.getMonthValue();
        int day = ldt.getDayOfMonth();
        int hour = ldt.getHour();
        int minute = ldt.getMinute();
        int second = ldt.getSecond();

        StringBuilder SQLTime = new StringBuilder();
        SQLTime.append(year + "-");
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(month) + "-");
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(day) + " ");
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(hour) + ":");
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(minute) + ":");
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(second));
        return SQLTime.toString();
    }

    /**
     * Convert a SQLite DateTime string to a Java LocalDateTime object
     * @param SQLDateTime The SQLite DateTime string
     * @return The Java LocalDateTime object
     */
    public static LocalDateTime SQLDateTimeToJavaDateTime(String SQLDateTime){
        // Split on the initial space
        String[] parts = SQLDateTime.split(" ");
        // Split the left side on the dash
        String[] ymd = parts[0].split("-");
        // Split the right side on the colon
        String[] hms = parts[1].split(":");

        int year = Integer.valueOf(ymd[0]);
        int month = Integer.valueOf(ymd[1]);
        int day = Integer.valueOf(ymd[2]);
        int hour = Integer.valueOf(hms[0]);
        int minute = Integer.valueOf(hms[1]);
        int second = Integer.valueOf(hms[2]);

        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    /**
     * Build an SQLite compatible date-time string based on numeric values
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @return The SQLite compatible date-time string
     */
    public static String getDateStringFromValues(int year, int month, int day,
                                                 int hour, int minute, int second){
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        sb.append("-");
        sb.append(getStringFromValueWithZeroWhereNecessary(month));
        sb.append("-");
        sb.append(getStringFromValueWithZeroWhereNecessary(day));
        sb.append(" ");
        sb.append(getStringFromValueWithZeroWhereNecessary(hour));
        sb.append(":");
        sb.append(getStringFromValueWithZeroWhereNecessary(minute));
        sb.append(":");
        sb.append(getStringFromValueWithZeroWhereNecessary(second));
        return sb.toString();
    }

    private static String getStringFromValueWithZeroWhereNecessary(int value){
        String val;
        if(value < 10)
            val = "0" + Integer.toString(value);
        else
            val = Integer.toString(value);
        return val;
    }
}
