package com.classsync.booking.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classsync.booking.dtos.request.AddSessionRequest;
import com.classsync.booking.dtos.request.CreateOfferingRequest;
import com.classsync.booking.dtos.response.OfferingResponse;
import com.classsync.booking.dtos.response.SessionResponse;
import com.classsync.booking.entity.Course;
import com.classsync.booking.entity.Offering;
import com.classsync.booking.entity.Session;
import com.classsync.booking.entity.User;
import com.classsync.booking.exception.BusinessException;
import com.classsync.booking.exception.ResourceNotFoundException;
import com.classsync.booking.repository.CourseRepository;
import com.classsync.booking.repository.OfferingRepository;
import com.classsync.booking.repository.SessionRepository;
import com.classsync.booking.repository.UserRepository;
import com.classsync.booking.service.TeacherService;
import com.classsync.booking.util.TimezoneUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final OfferingRepository offeringRepository;
    private final SessionRepository  sessionRepository;
    private final UserRepository     userRepository;
    private final CourseRepository   courseRepository;

   
    @Override
    @Transactional
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", request.getTeacherId()));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new BusinessException(
                "User " + request.getTeacherId() + " is not a TEACHER. " +
                "Only teachers can create offerings.");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", request.getCourseId()));

        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .title(request.getTitle())
                .description(request.getDescription())
                .maxCapacity(request.getMaxCapacity())
                .status(Offering.Status.ACTIVE)
                .build();

        Offering saved = offeringRepository.save(offering);
        log.info("Teacher {} created offering {} for course {}", teacher.getId(), saved.getId(), course.getId());

        return toOfferingResponse(saved, List.of(), teacher.getTimezone());
    }

  
    @Override
    @Transactional
    public SessionResponse addSession(Long offeringId, AddSessionRequest request) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> ResourceNotFoundException.of("Offering", offeringId));

        if (offering.getStatus() != Offering.Status.ACTIVE) {
            throw new BusinessException(
                "Cannot add sessions to a " + offering.getStatus() + " offering.");
        }

        LocalDateTime startUtc = TimezoneUtil.toUtc(request.getStartTime());
        LocalDateTime endUtc   = TimezoneUtil.toUtc(request.getEndTime());

        if (!endUtc.isAfter(startUtc)) {
            throw new BusinessException(
                "endTime must be after startTime. Got startTime=" + startUtc + ", endTime=" + endUtc);
        }

        if (startUtc.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot add a session in the past.");
        }

        Session session = Session.builder()
                .offering(offering)
                .teacherId(offering.getTeacher().getId())   
                .startTime(startUtc)
                .endTime(endUtc)
                .build();

        Session saved = sessionRepository.save(session);
        log.info("Added session {} to offering {} (UTC: {} to {})",
                 saved.getId(), offeringId, startUtc, endUtc);

        String teacherTz = offering.getTeacher().getTimezone();
        return toSessionResponse(saved, teacherTz);
    }

   
    @Override
    @Transactional(readOnly = true)
    public List<OfferingResponse> getTeacherOfferings(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("Teacher", teacherId));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new BusinessException("User " + teacherId + " is not a TEACHER.");
        }

        List<Offering> offerings = offeringRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        return offerings.stream()
                .map(o -> {
                    List<Session> sessions = sessionRepository
                            .findByOfferingIdOrderByStartTimeAsc(o.getId());
                    return toOfferingResponse(o, sessions, teacher.getTimezone());
                })
                .collect(Collectors.toList());
    }

    
    public static OfferingResponse toOfferingResponse(
            Offering offering, List<Session> sessions, String viewerTimezone) {

        List<SessionResponse> sessionResponses = sessions.stream()
                .map(s -> toSessionResponse(s, viewerTimezone))
                .collect(Collectors.toList());

        return OfferingResponse.builder()
                .id(offering.getId())
                .courseId(offering.getCourse().getId())
                .courseTitle(offering.getCourse().getTitle())
                .teacherId(offering.getTeacher().getId())
                .teacherName(offering.getTeacher().getName())
                .teacherTimezone(offering.getTeacher().getTimezone())
                .title(offering.getTitle())
                .description(offering.getDescription())
                .maxCapacity(offering.getMaxCapacity())
                .status(offering.getStatus().name())
                .totalSessions(sessions.size())
                .createdAt(offering.getCreatedAt())
                .sessions(sessionResponses)
                .build();
    }

   
    public static SessionResponse toSessionResponse(Session session, String viewerTimezone) {
        return SessionResponse.builder()
                .id(session.getId())
                .offeringId(session.getOffering().getId())
                .teacherId(session.getTeacherId())
                .startTimeLocal(TimezoneUtil.toUserTimezone(session.getStartTime(), viewerTimezone))
                .endTimeLocal(TimezoneUtil.toUserTimezone(session.getEndTime(), viewerTimezone))
                .startTimeUtc(TimezoneUtil.asUtcZoned(session.getStartTime()))
                .endTimeUtc(TimezoneUtil.asUtcZoned(session.getEndTime()))
                .timezone(viewerTimezone)
                .build();
    }
}
