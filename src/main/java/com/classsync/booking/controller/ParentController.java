package com.classsync.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.classsync.booking.dtos.request.BookOfferingRequest;
import com.classsync.booking.dtos.response.ApiResponse;
import com.classsync.booking.dtos.response.BookingResponse;
import com.classsync.booking.dtos.response.OfferingResponse;
import com.classsync.booking.service.ParentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    
    @GetMapping("/offerings")
    public ResponseEntity<ApiResponse<List<OfferingResponse>>> getAvailableOfferings(
            @RequestParam(required = false) Long parentId) {

        List<OfferingResponse> offerings = parentService.getAvailableOfferings(parentId);
        return ResponseEntity.ok(ApiResponse.ok(offerings));
    }

    
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> bookOffering(
            @Valid @RequestBody BookOfferingRequest request) {

        BookingResponse response = parentService.bookOffering(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    
    @GetMapping("/{parentId}/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getParentBookings(
            @PathVariable Long parentId) {

        List<BookingResponse> bookings = parentService.getParentBookings(parentId);
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }
}