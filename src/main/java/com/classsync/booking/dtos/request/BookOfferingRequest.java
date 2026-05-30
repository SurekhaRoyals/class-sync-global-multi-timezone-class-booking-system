package com.classsync.booking.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookOfferingRequest {

    @NotNull(message = "parentId is required")
    private Long parentId;

    @NotNull(message = "offeringId is required")
    private Long offeringId;
}