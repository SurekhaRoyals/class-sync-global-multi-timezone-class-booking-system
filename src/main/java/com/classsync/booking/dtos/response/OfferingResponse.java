package com.classsync.booking.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OfferingResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long teacherId;
    private String teacherName;
    private String teacherTimezone;
    private String title;
    private String description;
    private int maxCapacity;
    private String status;
    private int totalSessions;
    private LocalDateTime createdAt;

    /** Populated when fetching with sessions (e.g., teacher view) */
    private List<SessionResponse> sessions;
}
