package com.classsync.booking.config;


import com.classsync.booking.ClassSyncGlobalMultiTimezoneClassBookingSystemApplication;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class JacksonConfig {

    private final ClassSyncGlobalMultiTimezoneClassBookingSystemApplication classSyncGlobalMultiTimezoneClassBookingSystemApplication;

    JacksonConfig(ClassSyncGlobalMultiTimezoneClassBookingSystemApplication classSyncGlobalMultiTimezoneClassBookingSystemApplication) {
        this.classSyncGlobalMultiTimezoneClassBookingSystemApplication = classSyncGlobalMultiTimezoneClassBookingSystemApplication;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        mapper.configOverride(LocalDateTime.class)
        .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ss"));

  return mapper;
    }
}