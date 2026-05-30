package com.classsync.booking.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long offeringId;
    private String offeringTitle;
    private String courseTitle;
    private Long teacherId;
    private String teacherName;
    private Long parentId;
    private String status;
    private LocalDateTime bookedAt;

   
    private List<SessionResponse> sessions;
}
