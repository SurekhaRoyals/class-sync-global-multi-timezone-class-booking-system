CREATE DATABASE IF NOT EXISTS class_bookings CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE class_bookings;

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    role        ENUM('TEACHER','PARENT') NOT NULL,
    timezone    VARCHAR(60)   NOT NULL DEFAULT 'UTC',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL,
    description TEXT,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE offerings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT        NOT NULL,
    teacher_id  BIGINT        NOT NULL,
    title       VARCHAR(200)  NOT NULL,           
    description TEXT,
    max_capacity INT          NOT NULL DEFAULT 30,
    status      ENUM('ACTIVE','CANCELLED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_offering_course  FOREIGN KEY (course_id)  REFERENCES courses(id),
    CONSTRAINT fk_offering_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE INDEX idx_offerings_teacher ON offerings(teacher_id);
CREATE INDEX idx_offerings_course  ON offerings(course_id);
CREATE INDEX idx_offerings_status  ON offerings(status);

CREATE TABLE sessions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    offering_id BIGINT        NOT NULL,
    teacher_id  BIGINT        NOT NULL,           
    start_time  DATETIME      NOT NULL,           
    end_time    DATETIME      NOT NULL,           
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),
    CONSTRAINT fk_session_teacher  FOREIGN KEY (teacher_id)  REFERENCES users(id),
    CONSTRAINT chk_session_times   CHECK (end_time > start_time)
);

CREATE INDEX idx_sessions_offering   ON sessions(offering_id);
CREATE INDEX idx_sessions_time_range ON sessions(start_time, end_time);  

CREATE TABLE bookings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    offering_id BIGINT        NOT NULL,
    parent_id   BIGINT        NOT NULL,
    status      ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),
    CONSTRAINT fk_booking_parent   FOREIGN KEY (parent_id)   REFERENCES users(id),
    CONSTRAINT uq_booking_parent_offering UNIQUE (parent_id, offering_id)
);

CREATE INDEX idx_bookings_parent   ON bookings(parent_id);
CREATE INDEX idx_bookings_offering ON bookings(offering_id);

