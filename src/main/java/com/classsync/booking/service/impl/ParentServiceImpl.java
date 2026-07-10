package com.classsync.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.classsync.booking.dtos.request.BookOfferingRequest;
import com.classsync.booking.dtos.response.BookingResponse;
import com.classsync.booking.dtos.response.OfferingResponse;
import com.classsync.booking.dtos.response.SessionResponse;
import com.classsync.booking.entity.Booking;
import com.classsync.booking.entity.Offering;
import com.classsync.booking.entity.Session;
import com.classsync.booking.entity.User;
import com.classsync.booking.exception.BusinessException;
import com.classsync.booking.exception.ResourceNotFoundException;
import com.classsync.booking.exception.TimeConflictException;
import com.classsync.booking.repository.BookingRepository;
import com.classsync.booking.repository.OfferingRepository;
import com.classsync.booking.repository.SessionRepository;
import com.classsync.booking.repository.UserRepository;
import com.classsync.booking.service.ParentService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Slf4j
@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final OfferingRepository offeringRepository;
    private final SessionRepository  sessionRepository;
    private final BookingRepository  bookingRepository;
    private final UserRepository     userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OfferingResponse> getAvailableOfferings(Long parentId) {
        String viewerTimezone = "UTC";
        if (parentId != null) {
            User parent = userRepository.findById(parentId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Parent", parentId));
            viewerTimezone = parent.getTimezone();
        }

        final String tz = viewerTimezone;
        List<Offering> offerings = offeringRepository.findAllActiveWithDetails();

        return offerings.stream()
                .map(o -> {
                    List<Session> sessions = sessionRepository
                            .findByOfferingIdOrderByStartTimeAsc(o.getId());
                    return TeacherServiceImpl.toOfferingResponse(o, sessions, tz);
                })
                .collect(Collectors.toList());
    }

    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponse bookOffering(BookOfferingRequest request) {

        User parent = userRepository.findById(request.getParentId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", request.getParentId()));

        if (parent.getRole() != User.Role.PARENT) {
            throw new BusinessException(
                "User " + request.getParentId() + " is not a PARENT. " +
                "Only parents can book offerings.");
        }

        Offering offering = offeringRepository.findById(request.getOfferingId())
                .orElseThrow(() -> ResourceNotFoundException.of("Offering", request.getOfferingId()));

        if (offering.getStatus() != Offering.Status.ACTIVE) {
            throw new BusinessException(
                "Offering '" + offering.getTitle() + "' is " + offering.getStatus() +
                " and cannot be booked.");
        }

        List<Session> offeringSessions = sessionRepository
                .findByOfferingIdOrderByStartTimeAsc(offering.getId());

        if (offeringSessions.isEmpty()) {
            throw new BusinessException(
                "Offering '" + offering.getTitle() + "' has no sessions yet. " +
                "Please wait for the teacher to add sessions.");
        }
//         fast duplicate check before acquiring the lock
         boolean alreadyBooked = bookingRepository
                .existsByOfferingIdAndParentIdAndStatus(
                        offering.getId(), parent.getId(), Booking.Status.CONFIRMED);

        if (alreadyBooked) {
            throw new BusinessException(
                "You have already booked offering '" + offering.getTitle());
        }
        
		/* Optional<Booking> booking = */ bookingRepository.findByOfferingIdAndParentIdWithLock(
                offering.getId(), parent.getId());

        List<Session> conflicts = sessionRepository.findConflictingSessions(
                parent.getId(), offering.getId());
        if (!conflicts.isEmpty()) {
            String conflictDetails = conflicts.stream()
                    .map(s -> String.format("Session on %s (offering id: %d)",
                            s.getStartTime().toLocalDate(),
                            s.getOffering().getId()))
                    .distinct()
                    .collect(Collectors.joining(", "));

            throw new TimeConflictException(
                "Cannot book '" + offering.getTitle() + "'. " +
                "Time conflict with already-booked sessions: " + conflictDetails);
        }

        Booking booking = Booking.builder()
                .offering(offering)
                .parent(parent)
                .status(Booking.Status.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);
       
        log.info("Parent {} successfully booked offering {} ({} sessions)",
                 parent.getId(), offering.getId(), offeringSessions.size());

        return toBookingResponse(saved, offeringSessions, parent.getTimezone());
    }

   
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getParentBookings(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Parent", parentId));

        if (parent.getRole() != User.Role.PARENT) {
            throw new BusinessException("User " + parentId + " is not a PARENT.");
        }

        List<Booking> bookings = bookingRepository.findByParentIdWithDetails(parentId);

        return bookings.stream()
                .map(b -> {
                    List<Session> sessions = sessionRepository
                            .findByOfferingIdOrderByStartTimeAsc(b.getOffering().getId());
//                    parent can see all session times in their own timezone
                    return toBookingResponse(b, sessions, parent.getTimezone());
                })
                .collect(Collectors.toList());
    }

   
    private BookingResponse toBookingResponse(
            Booking booking, List<Session> sessions, String parentTimezone) {

        List<SessionResponse> sessionResponses = sessions.stream()
                .map(s -> TeacherServiceImpl.toSessionResponse(s, parentTimezone))
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .offeringId(booking.getOffering().getId())
                .offeringTitle(booking.getOffering().getTitle())
                .courseTitle(booking.getOffering().getCourse().getTitle())
                .teacherId(booking.getOffering().getTeacher().getId())
                .teacherName(booking.getOffering().getTeacher().getName())
                .parentId(booking.getParent().getId())
                .status(booking.getStatus().name())
                .bookedAt(booking.getBookedAt())
                .sessions(sessionResponses)
                .build();
    }
}