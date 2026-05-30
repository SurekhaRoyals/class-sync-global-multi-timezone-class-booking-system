package com.classsync.booking.service;

import java.util.List;

import com.classsync.booking.dtos.request.BookOfferingRequest;
import com.classsync.booking.dtos.response.BookingResponse;
import com.classsync.booking.dtos.response.OfferingResponse;


public interface ParentService {

   
    List<OfferingResponse> getAvailableOfferings(Long parentId);

    
    BookingResponse bookOffering(BookOfferingRequest request);

    
    List<BookingResponse> getParentBookings(Long parentId);
}
