package com.classsync.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions", indexes = {
        @Index(name = "idx_sessions_offering",   columnList = "offering_id"),
        @Index(name = "idx_sessions_time_range", columnList = "start_time, end_time")
})
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;


    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

   
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
