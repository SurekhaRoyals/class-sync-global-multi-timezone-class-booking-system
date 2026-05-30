package com.classsync.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classsync.booking.entity.Session;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByOfferingIdOrderByStartTimeAsc(Long offeringId);

    
    @Query("""
        SELECT s FROM Session s
        WHERE s.offering.id IN (
            SELECT b.offering.id FROM Booking b
            WHERE b.parent.id = :parentId
              AND b.status = 'CONFIRMED'
        )
        AND EXISTS (
            SELECT cs FROM Session cs
            WHERE cs.offering.id = :candidateOfferingId
              AND cs.startTime < s.endTime
              AND s.startTime   < cs.endTime
        )
    """)
    List<Session> findConflictingSessions(
            @Param("parentId") Long parentId,
            @Param("candidateOfferingId") Long candidateOfferingId);

    
    @Query("""
        SELECT s FROM Session s
        WHERE s.offering.id IN (
            SELECT b.offering.id FROM Booking b
            WHERE b.parent.id = :parentId
              AND b.status = 'CONFIRMED'
        )
        ORDER BY s.startTime ASC
    """)
    List<Session> findAllBookedSessionsForParent(@Param("parentId") Long parentId);
}