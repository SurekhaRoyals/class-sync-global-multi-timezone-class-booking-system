package com.classsync.booking.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOfferingRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotNull(message = "teacherId is required")
    private Long teacherId;

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @Min(value = 1, message = "maxCapacity must be at least 1")
    private int maxCapacity = 30;
}