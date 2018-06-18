/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object TimeUtils{
    /**
     * Return the utc seconds since epoch from LocalDateTime
     * NOTE: This function intentionally returns uncorrected UTC seconds. It assumes that the LocalDateTime object is
     * created with the intention of being zoned in 'UTC'. This is only intended to be used for selection queries so
     * users don't need to worry about their time zone vs that of UTC
     * @param ldt The LocalDateTime object
     * @return The intentionally uncorrected, utc zoned, seconds since epoch.
     */
    @JvmStatic fun utcSecondsFromLDT_SEL(ldt: LocalDateTime): Long {
        return utcSecondsFromValues_SEL(ldt.year, ldt.monthValue, ldt.dayOfMonth, ldt.hour, ldt.minute, ldt.second)
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
    @JvmStatic fun utcSecondsFromValues_SEL(year: Int, month: Int, day: Int,
                                            hour: Int, minute: Int, second: Int): Long {
        val ldt = LocalDateTime.of(year, month, day, hour, minute, second)
        val zdt = ldt.atZone(ZoneId.of("UTC"))
        return zdt.toEpochSecond()
    }

    /**
     * Convert a utc timestamp to a LocalDateTime object using the default system timezone
     * @param utcSeconds
     * @return The LocalDateTime object
     */
    @JvmStatic fun utcSecondsToLDT(utcSeconds: Long): LocalDateTime {
        val i = Instant.ofEpochSecond(utcSeconds)
        val zdt = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId())
        return zdt.toLocalDateTime()
    }

    /**
     * Used only for printing values
     * @param utcSeconds
     * @return
     */
    @JvmStatic fun utcSecondsToZDT(utcSeconds: String): String {
        val utc: Long
        try {
            utc = utcSeconds.toLong()
        }
        catch(e: NumberFormatException){
            return "0"
        }
        val i = Instant.ofEpochSecond(utc)
        return ZonedDateTime.ofInstant(i, ZoneId.of("UTC")).toString()
    }

    /**
     * Convert a seconds from epoch UTC string to a LocalDateTime object
     * @param utcSeconds The seconds from epoch string
     * @return The LocalDateTime object
     */
    @JvmStatic fun utcSecondsToLDT(utcSeconds: String): LocalDateTime {
        val utc: Long
        try{
            utc = utcSeconds.toLong()
        }
        catch(e: NumberFormatException){
            return utcSecondsToLDT(0L)
        }
        return utcSecondsToLDT(utc)
    }

    /**
     * Convert a LocalDateTime object to UTC seconds
     * @param ldt The local date time object
     * @return the UTC time as a long
     */
    @JvmStatic fun LDTtoUTCSeconds(ldt: LocalDateTime): Long {
        val zdt = ldt.atZone(ZoneId.systemDefault())
        return zdt.withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()
    }


    @JvmStatic fun sqlTimeToUTC(sqlTime: String): Long {
        return SQLDateTimeToJavaDateTime(sqlTime).atZone(ZoneId.systemDefault()).toEpochSecond()
    }

    /**
     * Convert a java LocalDateTime object to an SQLite DateTime compatible object
     * @param ldt
     * @return The SQLite DateTime compatible time string
     */
    @JvmStatic fun javaDateTimeToSQLDateTime(ldt: LocalDateTime): String {
        val year = ldt.year
        val month = ldt.monthValue
        val day = ldt.dayOfMonth
        val hour = ldt.hour
        val minute = ldt.minute
        val second = ldt.second

        var SQLTime = StringBuilder()
        SQLTime.append(year.toString() + "-")
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(month)).append("-")
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(day)).append(" ")
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(hour)).append(":")
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(minute)).append(":")
        SQLTime.append(getStringFromValueWithZeroWhereNecessary(second))
        return SQLTime.toString()
    }

    /**
     * Convert a SQLite DateTime string to a Java LocalDateTime object
     * @param SQLDateTime The SQLite DateTime string
     * @return The Java LocalDateTime object
     */
    @JvmStatic fun SQLDateTimeToJavaDateTime(SQLDateTime: String): LocalDateTime {
        val parts = SQLDateTime.split(" ")
        val ymd = parts[0].split("-")
        val hms = parts[1].split(":")

        val year = ymd[0].toInt()
        val month = ymd[1].toInt()
        val day = ymd[2].toInt()
        val hour = hms[0].toInt()
        val minute = hms[1].toInt()
        val second = hms[2].toInt()

        return LocalDateTime.of(year, month, day, hour, minute, second)
    }

    private fun getStringFromValueWithZeroWhereNecessary(value: Int): String {
        val vals: String

        try{
            vals = value.toString()
        }
        catch(e: NumberFormatException){
            return "0"
        }

        val zero = "0"
        if(value < 10)
            return zero + vals
        return vals
    }
}
