package com.classsync.booking.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
@Builder
public class SessionResponse {

    private Long id;
    private Long offeringId;
    private Long teacherId;

//   time in the user's timezone
    private ZonedDateTime startTimeLocal;
    private ZonedDateTime endTimeLocal;

//     UTC 
    private ZonedDateTime startTimeUtc;
    private ZonedDateTime endTimeUtc;

    private String timezone; // which timezone startTimeLocal is expressed in
}
