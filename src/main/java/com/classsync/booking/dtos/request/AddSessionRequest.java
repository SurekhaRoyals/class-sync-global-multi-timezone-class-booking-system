package com.classsync.booking.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
public class AddSessionRequest {

    @NotNull(message = "startTime is required (ISO-8601 with timezone, e.g. 2025-06-07T18:00:00-05:00)")
    private ZonedDateTime startTime;

    @NotNull(message = "endTime is required (ISO-8601 with timezone, e.g. 2025-06-07T19:00:00-05:00)")
    private ZonedDateTime endTime;
}