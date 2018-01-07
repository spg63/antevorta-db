/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction;

import java.time.*;
import java.util.TimeZone;

/**
 * Class to convert to and from seconds from epoch UTC, Java LocalDateTime and SQLite datetime
 * objects
 */
public class TimeUtils {

    /**
     * Return the utc seconds since epoch from LocalDateTime
     * NOTE: This function intentionally returns uncorrected UTC seconds. It assumes that the LocalDateTime object is
     * created with the intention of being zoned in 'UTC'. This is only intended to be used for selection queries so
     * users don't need to worry about their time zone vs that of UTC
     * @param ldt The LocalDateTime object
     * @return The intentionally uncorrected, utc zoned, seconds since epoch.
     */
    public static long utcSecondsFromLDT_SEL(LocalDateTime ldt){
        return utcSecondsFromValues_SEL(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
                ldt.getHour(), ldt.getMinute(), ldt.getSecond());
    }

    /**
     * Used *ONLY* for Selector Time selection. It *intentionally* returns an incorrectly zoned UTC value so a selection
     * statement can be used using local time of midnight and select values from UTC midnight instead of the user
     * calculating the time offset.
     * NOTE: If you need to get the correct UTC value, including offset from system defaul, use the LDTtoUTCSeconds
     * function found below this one
     * @param year The year
     * @param month The month
     * @param day The day
     * @param hour The hour
     * @param minute The minute
     * @param second The second
     * @return An intentionally uncorrected utc value from the system local time
     */
    public static long utcSecondsFromValues_SEL(int year, int month, int day,
                                                int hour, int minute, int second){
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));
        return zdt.toEpochSecond();
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
     * Used only for printing values
     * @param utcSeconds
     * @return
     */
    public static String utcSecondsToZDT(String utcSeconds){
        long utc = Long.parseLong(utcSeconds);
        Instant i = Instant.ofEpochSecond(utc);
        return ZonedDateTime.ofInstant(i, ZoneId.of("UTC")).toString();
    }

    /**
     * Convert a seconds from epoch UTC string to a LocalDateTime object
     * @param utcSeconds The seconds from epoch string
     * @return The LocalDateTime object
     */
    public static LocalDateTime utcSecondsToLDT(String utcSeconds){
        return utcSecondsToLDT(Long.parseLong(utcSeconds));
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



    private static String getStringFromValueWithZeroWhereNecessary(int value){
        String val;
        if(value < 10)
            val = "0" + Integer.toString(value);
        else
            val = Integer.toString(value);
        return val;
    }
}
