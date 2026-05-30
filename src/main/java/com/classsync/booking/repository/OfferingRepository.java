package com.classsync.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classsync.booking.entity.Offering;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {

    List<Offering> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    @Query("""
        SELECT DISTINCT o FROM Offering o
        JOIN FETCH o.course
        JOIN FETCH o.teacher
        WHERE o.status = 'ACTIVE'
        ORDER BY o.createdAt DESC
    """)
    List<Offering> findAllActiveWithDetails();
}
