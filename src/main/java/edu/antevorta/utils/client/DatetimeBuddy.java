/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.utils.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;

/**
 * @author Andrew W.E. McDonald
 *
 * Maybe this should be changed to have an 'active' DateTimeFormatter...
 * then the functions could just call the 'activeDTF', and there would be fewer functions.
 */
@SuppressWarnings("ALL")
public class DatetimeBuddy {

   ZonedDateTime zdt;
   LocalDateTime ldt;

   static HashMap<String, DateTimeFormatter> dtFormats = null;
   static final String decreasing = "decreasing";
   static final String decreasingWithZone = "decreasingWithZone";
   static final String decreasingWithZoneOffset = "decreasingWithZoneOffset";
   static final String decreasingWithZoneId = "decreasingWithZoneId";


   final static ZoneId zoneUTC = ZoneId.of("UTC");


   public DatetimeBuddy() {
      if (dtFormats == null) {
         initDtFormats();
      }

   }

      /* this is an odd place to put this. but it's an odd function, so, here it is... */

   public ZonedDateTime getZDT(int year, int month, int day, int hour, int minute,
                               int second, int nanoOfSecond, ZoneId zid) {
      return ZonedDateTime.of(year, month, day, hour, minute, second, nanoOfSecond, zid);
   }

   /* these don't have time zones. */


   /**
    * No time zone.
    * @param year
    * @param month
    * @param day
    * @param hour
    * @param minute
    * @param second
    * @return
    */
   public LocalDateTime getLDT(int year, int month, int day, int hour, int minute, int second) {
      return LocalDateTime.of(year, month, day, hour, minute, second);
   }

   /**
    * No time zone.
    *
    * input format is 'decreasing' which means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * @param dateTimeIn
    * @return
    */
   public LocalDateTime getLDTfromDecreasing(String dateTimeIn){
      return LocalDateTime.parse(dateTimeIn, dtFormats.get(decreasing));
   }

   /**
    * No time zone.
    *
    * output format is 'decreasing' which means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * @param ldt
    * @return
    */
   public String getDecreasing(LocalDateTime ldt){
      return ldt.format(dtFormats.get("decreasing"));
   }

   /*
   These deal with using the 'decreasing' format, with an 'implied' timezeone,
   meaning, 'decreasing' format. So, there isn't a timezone in the input String.
    */

   /**
    * input format is 'decreasing' which means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    *
    * @param dateTimeIn
    * @param impliedZone
    * @return
    */
   public ZonedDateTime getZDTfromDecreasingWithImpliedZone(String dateTimeIn, ZoneId impliedZone){
      ldt = LocalDateTime.parse(dateTimeIn, dtFormats.get(decreasing));
      return ldt.atZone(impliedZone);
   }

   /**
    * LocalDateTime from the seconds input, assuming it has an implied time zone of 'impliedZone'
    * @param seconds
    * @param impliedZone
    * @return
    */
   public LocalDateTime getLDTfromSeconds(long seconds, ZoneId impliedZone){
       zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), impliedZone);
       return zdt.toLocalDateTime();
   }



   /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * 'withOffset' means after seconds, there's a zone offset (e.g., +00, -05, etc.)
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    *
    * @param dateTimeIn
    * @param inputImpliedZone
    * @param outputZone
    * @return
    */
   public String getDecreasingWithOffsetFromDecreasingWithInputZoneImplied(String dateTimeIn,
                                                                           ZoneId inputImpliedZone,
                                                                           ZoneId outputZone){
       zdt = getZDTfromDecreasingWithImpliedZone(dateTimeIn, inputImpliedZone);
      return zdt.format(dtFormats.get(decreasingWithZoneOffset).withZone(outputZone));
   }

   /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    *
    *
    * @param dateTimeIn
    * @param inputImpliedZone
    * @param outputZone
    * @return
    */
   public long getMillisFromDecreasingWithInputZoneImplied(String dateTimeIn, ZoneId inputImpliedZone,
                                                           ZoneId outputZone){
      zdt = getZDTfromDecreasingWithImpliedZone(dateTimeIn, inputImpliedZone);
      return getMillisWithZone(zdt, outputZone);
   }

   /**
    * * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    *
    * @param dateTimeIn
    * @param inputImpliedZone
    * @param outputZone
    * @return
    */
   public long getSecondsFromDecreasingWithInputZoneImplied(String dateTimeIn, ZoneId inputImpliedZone,
                                                            ZoneId outputZone){
      zdt = getZDTfromDecreasingWithImpliedZone(dateTimeIn, inputImpliedZone);
      return getSecondsWithZone(zdt, outputZone);
   }

   /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * 'withOffset' means after seconds, there's a zone offset (e.g., +00, -05, etc.)
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    * @param millis
    * @param inputImpliedZone
    * @param outputZone
    * @return
    */
   public String getDecreasingWithOffsetFromMillisWithInputZoneImplied(long millis, ZoneId inputImpliedZone,
                                                                       ZoneId outputZone){
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), inputImpliedZone);
      return zdt.format(dtFormats.get(decreasing).withZone(outputZone));
   }

   /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    *
    * as the input zone is 'implied', it's not explicitly stated in the input dateTimeIn string.
    *
    * @param millis
    * @param inputImpliedZone
    * @param outputZone
    * @return
    */
   public String getDecreasingFromSecondsWithInputZoneImplied(long millis, ZoneId inputImpliedZone,
                                                              ZoneId outputZone){
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), inputImpliedZone);
      return zdt.format(dtFormats.get(decreasing).withZone(outputZone));
   }


   /*
        These are for Strings (input or output) that use a zone offset to specify time zone, e.g: -05 for EDT,
        +00 for UTC, etc.
   */


   /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * 'withOffset' means after seconds, there's a zone offset (e.g., +00, -05, etc.)
    *
    * @param dateTimeIn
    * @param outputZone
    * @return
    */
   public long getMillisFromDecreasingWithZoneOffset(String dateTimeIn, ZoneId outputZone){
      zdt = zdt.parse(dateTimeIn, dtFormats.get(decreasingWithZoneOffset));
      return getMillisWithZone(zdt, outputZone);
   }


    /**
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * 'withOffset' means after seconds, there's a zone offset (e.g., +00, -05, etc.)
    *
    * @param millis
    * @param inputZone
    * @param outputZone
    * @return
    */
   public String getDecreasingZoneOffsetFromMillis(long millis, ZoneId inputZone, ZoneId outputZone){
       zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), inputZone);
       return zdt.format(dtFormats.get(decreasingWithZoneOffset).withZone(outputZone));
   }


   /*
   These deal with 'Local' times
   */

   /**
    * This takes some millis time and the time zone that time is associated with.
    *
    * @param millis
    * @param inputZone
    * @return a string formatted according to 'decreasing' (e.g.: 2015-02-02 05:54:00),
    * so, it will then have an 'implied' time zone of 'inputZone' (see the implied methods further up).
    */
   public String getLocalDecreasingFromMillis(long millis, ZoneId inputZone){
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), inputZone);
      return zdt.toLocalDateTime().format(dtFormats.get(decreasing));
   }

   /**
    * returns input 'millis' (so, milliseconds) converted to a 'decreasing' datetime string,
    * assuming the timezone is 'inputZone'
    *
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    * @param millis
    * @return
    */
   public String getSystemLocalDecreasingFromMillis(long millis){
     return getLocalDecreasingFromMillis(millis, ZoneId.systemDefault());
   }


   /**
    * returns input 'seconds' converted to a 'decreasing' datetime string,
    * assuming the timezone is 'inputZone'
    *
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    *
    * @param seconds
    * @param inputZone
    * @return
    */
   public String getLocalDecreasingFromSeconds(long seconds, ZoneId inputZone){
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), inputZone);
      return zdt.toLocalDateTime().format(dtFormats.get(decreasing));
   }

   /**
    * returns input 'seconds' converted to a 'decreasing' datetime string,
    * assuming the timezone is the system default (as per ZoneId.systemDefault())
    *
    * 'decreasing' means the following format: uuuu-MM-dd HH:mm:ss
    * e.g., 2015-02-02 10:54:00
    *
    *
    * @param seconds
    * @return
    */
   public String getSystemLocalDecreasingFromSeconds(long seconds){
     return getLocalDecreasingFromSeconds(seconds, ZoneId.systemDefault());
   }


   /**
    * These allow easily formatting ZonedDateTime and LocalDateTime objects.
    *
    * See: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
    */
   private void initDtFormats(){
      int numFormatters = 10; // just a number.
       dtFormats = new HashMap<>(numFormatters);

      // e.g. 2015-02-02 05:54:00
      String decreasingStrFmt = "uuuu-MM-dd HH:mm:ss";
      dtFormats.put(decreasing, DateTimeFormatter.ofPattern(decreasingStrFmt));

      // e.g. 2015-02-02 05:54:00 (EST)
      String decreasingWithZoneStrFmt = "uuuu-MM-dd HH:mm:ss (zzz)";
      dtFormats.put(decreasingWithZone, DateTimeFormatter.ofPattern(decreasingWithZoneStrFmt));

      // e.g. 2015-02-02 05:54:00-05
      String decreasingWithZoneOffsetStrFmt = "uuuu-MM-dd HH:mm:ssx";
      dtFormats.put(decreasingWithZoneOffset, DateTimeFormatter.ofPattern(decreasingWithZoneOffsetStrFmt));

      // e.g. 2015-02-02 05:54:00America/New_York
      String decreasingWithZoneIdStrFmt = "uuuu-MM-dd HH:mm:ss VV";
      dtFormats.put(decreasingWithZoneId, DateTimeFormatter.ofPattern(decreasingWithZoneOffsetStrFmt));

   }

   /**
    * converts a LocalDateTime to UTC epoch-based millis
    * @param ldt
    * @return
    */
   public long getUTCmillis(LocalDateTime ldt) {
      return getMillisWithZone(ldt, zoneUTC);
   }


   /**
    * converts a ZonedDateTime to UTC epoch-based millis
     * @param zdt
    * @return
    */
   public long getUTCmillis(ZonedDateTime zdt) {
       return getMillisWithZone(zdt, zoneUTC);
   }

   /**
    * converts a LocalDateTime to UTC epoch-based seconds
    * @param ldt
    * @return
    */
   public long getUTCseconds(LocalDateTime ldt) {
      return getSecondsWithZone(ldt, zoneUTC);
   }

   /**
    * converts a ZonedDateTime to UTC epoch-based seconds
    * @param zdt
    * @return
    */
   public long getUTCseconds(ZonedDateTime zdt){
       return getSecondsWithZone(zdt, zoneUTC);
    }

   /**
    * returns millis converted from a LocalDateTime, which have time zone 'zid' after conversion.
    * @param ldt
    * @param zid
    * @return
    */
    public long getMillisWithZone(LocalDateTime ldt, ZoneId zid) {
      return ldt.atZone(zid).toInstant().toEpochMilli();
   }

   /**
    * returns millis converted from a ZonedDateTime, which have time zone 'zid' after conversion.
    * @param zdt
    * @param zid
    * @return
    */
   public long getMillisWithZone(ZonedDateTime zdt, ZoneId zid) {
      return zdt.withZoneSameInstant(zid).toInstant().toEpochMilli();
   }


   /**
    * returns seconds converted from a ZonedDateTime, which have time zone 'zid' after conversion.
    * @param ldt
    * @param zid
    * @return
    */
   public long getSecondsWithZone(LocalDateTime ldt, ZoneId zid) {
      return ldt.atZone(zid).toEpochSecond();
   }

    /**
    * returns seconds converted from a ZonedDateTime, which have time zone 'zid' after conversion.
    * @param zdt
    * @param zid
    * @return
    */
   public long getSecondsWithZone(ZonedDateTime zdt, ZoneId zid){
      return zdt.withZoneSameInstant(zid).toEpochSecond();
    }

    /**
    * returns epoch time of iso instant string time/date, e.g.: 2011-12-03T10:15:30Z
    *
    * see: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_INSTANT
    *
    * @param isoInstantStr
    * @return
    */
   public long getMillisFromISOInstant(String isoInstantStr){
      TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(isoInstantStr);
      Instant instant = Instant.from(ta);
      return instant.toEpochMilli();
   }

}
