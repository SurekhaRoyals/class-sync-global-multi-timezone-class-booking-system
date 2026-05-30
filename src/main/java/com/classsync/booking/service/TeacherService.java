package com.classsync.booking.service;


import java.util.List;

import com.classsync.booking.dtos.request.AddSessionRequest;
import com.classsync.booking.dtos.request.CreateOfferingRequest;
import com.classsync.booking.dtos.response.OfferingResponse;
import com.classsync.booking.dtos.response.SessionResponse;


public interface TeacherService {

  
    OfferingResponse createOffering(CreateOfferingRequest request);

    SessionResponse addSession(Long offeringId, AddSessionRequest request);

    List<OfferingResponse> getTeacherOfferings(Long teacherId);
}