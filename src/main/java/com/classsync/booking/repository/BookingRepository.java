package com.classsync.booking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classsync.booking.entity.Booking;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b "
    		+ "WHERE b.offering.id = :offeringId "
    		+ "AND b.parent.id = :parentId")
    Optional<Booking> findByOfferingIdAndParentIdWithLock(
            @Param("offeringId") Long offeringId,
            @Param("parentId") Long parentId);

    
    
    boolean existsByOfferingIdAndParentIdAndStatus(
            Long offeringId, Long parentId, Booking.Status status);

  
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.offering o
        JOIN FETCH o.course
        JOIN FETCH o.teacher
        WHERE b.parent.id = :parentId
        ORDER BY b.bookedAt DESC
    """)
    List<Booking> findByParentIdWithDetails(@Param("parentId") Long parentId);
}
