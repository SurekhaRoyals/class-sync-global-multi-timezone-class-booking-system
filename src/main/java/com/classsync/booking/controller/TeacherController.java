package com.classsync.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.classsync.booking.dtos.request.AddSessionRequest;
import com.classsync.booking.dtos.request.CreateOfferingRequest;
import com.classsync.booking.dtos.response.ApiResponse;
import com.classsync.booking.dtos.response.OfferingResponse;
import com.classsync.booking.dtos.response.SessionResponse;
import com.classsync.booking.service.TeacherService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    
    @PostMapping("/offerings")
    public ResponseEntity<ApiResponse<OfferingResponse>> createOffering(
            @Valid @RequestBody CreateOfferingRequest request) {

        OfferingResponse response = teacherService.createOffering(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    
    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<ApiResponse<SessionResponse>> addSession(
            @PathVariable Long offeringId,
            @Valid @RequestBody AddSessionRequest request) {

        SessionResponse response = teacherService.addSession(offeringId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    
    @GetMapping("/{teacherId}/offerings")
    public ResponseEntity<ApiResponse<List<OfferingResponse>>> getTeacherOfferings(
            @PathVariable Long teacherId) {

        List<OfferingResponse> offerings = teacherService.getTeacherOfferings(teacherId);
        return ResponseEntity.ok(ApiResponse.ok(offerings));
    }
}