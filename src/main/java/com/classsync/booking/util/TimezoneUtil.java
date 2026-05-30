package com.classsync.booking.util;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public final class TimezoneUtil {

    private TimezoneUtil() {}

   
    public static LocalDateTime toUtc(ZonedDateTime zonedDateTime) {
        return zonedDateTime
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

   
//    public static ZonedDateTime toUserTimezone(LocalDateTime utcTime, String ianaTimezone) {
//        return utcTime
//                .atZone(ZoneId.of("UTC"))
//                .withZoneSameInstant(ZoneId.of(ianaTimezone));
//    }

    public static ZonedDateTime toUserTimezone(LocalDateTime utcTime, String ianaTimezone) {
        return utcTime.atZone(ZoneId.of("UTC"))        
                      .withZoneSameInstant(ZoneId.of(ianaTimezone));
    }
   
    public static ZonedDateTime asUtcZoned(LocalDateTime utcTime) {
        return utcTime.atZone(ZoneId.of("UTC"));
    }

   
    public static void validateTimezone(String timezone) {
        ZoneId.of(timezone); 
        }
}
