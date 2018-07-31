/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.utils.client;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Sean Grimes
 * @author Andrew W.E. McDonald
 */
@SuppressWarnings("ALL")
public class AntevortaDatetimeBuddy extends DatetimeBuddy {


    public long utcSecondsFromLDT_SEL(LocalDateTime ldt){
        return getUTCseconds(ldt);
    }

    public long utcSecondsFromValues_SEL(int year, int month, int day, int hour, int minute, int second){
       return getUTCseconds(getLDT(year, month, day, hour, minute, second));
    }

    public LocalDateTime utcSecondsToLDT(long seconds){
        return getLDTfromSeconds(seconds, zoneUTC);
    }

    public String utcSecondsToZDT(String utcSeconds){
        // this is a pretty ridiculous name, but it gets a 'decreasing' format, without any offset or millis,
        // accepts seconds, and assumes those seconds are in the timezone of the 2nd arg.
        return getDecreasingFromSecondsWithInputZoneImplied(Long.parseLong(utcSeconds), zoneUTC, zoneUTC);
    }

    public LocalDateTime utcSecondsToLDT(String utcSeconds){
        return getLDTfromSeconds(Long.parseLong(utcSeconds), zoneUTC);
    }

    public long LDTtoUTCSeconds(LocalDateTime ldt){
        return getUTCseconds(ldt);
    }

    public long sqlTimeToUTC(String sqlTime){
       return getSecondsFromDecreasingWithInputZoneImplied(sqlTime, ZoneId.systemDefault(), zoneUTC);
    }

    public String javaDateTimeToSQLDateTime(LocalDateTime ldt){
        return getDecreasing(ldt);
    }

    public LocalDateTime SQLDateTimeToJavaDateTime(String SQLDateTime){
        return getLDTfromDecreasing(SQLDateTime);
    }
}
