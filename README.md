[README.md](https://github.com/user-attachments/files/28425438/README.md)

# Global Class Booking System

A production-ready Spring Boot backend for a live-learning platform where teachers create class offerings and parents book them - handling timezones, schedule conflicts, and concurrent requests safely.


## Table of Contents
- <a href="#overview">Overview</a>
- <a href="#1-architecture">Architecture</a>
- <a href="#2-project-structure">Project Structure</a>
- <a href="#3-tech-stack">Tech Stack</a>
- <a href="#4-running-the-application">Running the Application</a>
- <a href="#5-api-reference">API Reference</a>
- <a href="#6-error-responses">Error Responses </a>
- <a href="#7-author-and-contact">Author & Contact</a>



<h2><a class="anchor" id="overview"></a>Overview</h2>

# ClassSync — Global Multi-Timezone Class Booking System

- ClassSync is a backend application designed for managing global live-learning class schedules and bookings across different timezones. The system allows teachers to create course offerings and schedule multiple live sessions, while parents and students can view and book entire offerings seamlessly from their local timezone.

- The platform handles timezone-aware session scheduling by storing all session timings in UTC and dynamically converting them based on the user’s location. It also includes robust conflict detection logic to prevent overlapping bookings and uses transactional concurrency handling to ensure data consistency during simultaneous booking requests.





<h2><a class="anchor" id="1-architecture"></a>1. Architecture</h2>

```text
Request
  │
  | 
Controller                  (parse HTTP, call service, wrap response)
  │
  |
Service                     (ALL business logic lives here)
  │  ├── TimezoneUtil       (UTC ↔ local conversion)
  │  └── ConflictDetect     (overlap query via SessionRepository)
  |
Repository                  (Spring Data JPA, custom JPQL queries)
  │
  |
MySQL                       (UTC DATETIME columns, indexes, UNIQUE constraints)

```

### Key design decisions

1. All timestamps stored in UTC
The sessions table uses plain DATETIME columns (no timezone info). By convention — enforced in code — every value written is UTC. TimezoneUtil handles all conversion. This avoids DST bugs and MySQL timezone misconfig issues.
2. Timezone shown in API responses
Teachers submit session times as ZonedDateTime (ISO-8601 with offset). The service converts to UTC for storage. On read, UTC is converted to the viewer's IANA timezone (Asia/Kolkata, America/New_York, etc.) so everyone sees their own local time.
3. Booking is at offering level
When a parent books an offering, all sessions are locked at once. The conflict check queries ALL sessions of ALL booked offerings for that parent.
4. Three-layer concurrency protection

- Layer 1: **`SELECT ... FOR UPDATE`** (pessimistic lock) serialises concurrent booking attempts
- Layer 2: JPQL overlap query with the formula **` a.start < b.end AND b.start < a.end `** .
- Layer 3:  **`UNIQUE(parent_id, offering_id)`** constraint catches any race condition that slips   through.

<h2><a class="anchor" id="2-project-structure"></a>2. Project Structure</h2>

```text

src/main/java/com/classplatform/
├── ClassBookingApplication.java     
├── config/
│   └── JacksonConfig.java           
├── controller/
│   ├── TeacherController.java       
│   └── ParentController.java        
├── dto/
│   ├── request/
│   │   ├── CreateOfferingRequest.java
│   │   ├── AddSessionRequest.java   
│   │   └── BookOfferingRequest.java
│   └── response/
│       ├── ApiResponse.java         
│       ├── OfferingResponse.java
│       ├── SessionResponse.java     
│       └── BookingResponse.java
├── entity/
│   ├── User.java                    
│   ├── Course.java
│   ├── Offering.java
│   ├── Session.java                
│   └── Booking.java                 
├── exception/
│   ├── ResourceNotFoundException.java  
│   ├── TimeConflictException.java      
│   ├── BusinessException.java         
│   └── GlobalExceptionHandler.java    
├── repository/
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   ├── OfferingRepository.java
│   ├── SessionRepository.java       
│   └── BookingRepository.java       
├── service/
│   ├── TeacherService.java          
│   ├── ParentService.java           
│   └── impl/
│       ├── TeacherServiceImpl.java
│       └── ParentServiceImpl.java   
└── util/
    └── TimezoneUtil.java            

```


<h2><a class="anchor" id="3-tech-stack"></a>4. Tech Stack</h2>

```bash
- Java 17+
- Spring Boot
- MySQL 8+
- Maven
```

<h2><a class="anchor" id="4-running-the-application"></a>3. Running the Application</h2>

### 1. Create the database
```sql
CREATE DATABASE class_bookings CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Then run **`src/main/resources/schema.sql`** to create tables and seed data.
 
### 2. Configure credentials
Set environment variables in **`application.properties`** file:
```bash

spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
```
### 3. Start

```bash 
mvn spring-boot:run
```
App starts on **`http://localhost:8080`**

### 4. Run tests
```bash
mvn test
```
Tests use H2 in-memory database — no MySQL needed.






<h2><a class="anchor" id="5-api-reference"></a>5. API Reference</h2>

## Teacher APIs
### Create Offering


```bash
POST /api/v1/teacher/offerings
Content-Type: application/json


{
  "teacherId": 1,
  "courseId": 1,
  "title": "Saturday Batch",
  "description": "Weekly Saturday sessions",
  "maxCapacity": 25
}

```

- Expected Response **`200`**:
```bash
{
  "success": true,
  "data": {
    "id": 1,
    "courseTitle": "Minecraft Coding",
    "teacherName": "Alice Teacher",
    "teacherTimezone": "America/New_York",
    "title": "Saturday Batch",
    "status": "ACTIVE",
    "totalSessions": 0
  }

}

```

### Add Session to Offering


```bash
POST /api/v1/teacher/offerings/{offeringId}/sessions
Content-Type: application/json

{
  "startTime": "2025-06-07T18:00:00-05:00",
  "endTime":   "2025-06-07T19:00:00-05:00"
}

```
Note: Submit times in your local timezone using ISO-8601 format with offset.

- Expexcted Response **`201`**:
```bash
{
  "success": true,
  "data": {
    "id": 1,
    "startTimeLocal": "2025-06-07T18:00:00-05:00",
    "endTimeLocal":   "2025-06-07T19:00:00-05:00",
    "startTimeUtc":   "2025-06-07T23:00:00Z",
    "endTimeUtc":     "2025-06-08T00:00:00Z",
    "timezone": "America/New_York"
  }
}

```

### Get Teacher's Offerings

```bash
GET /api/v1/teacher/{teacherId}/offerings
```

Returns all offerings with their sessions, times shown in teacher's timezone.

## Parent APIs

### Browse Available Offerings

```bash
GET /api/v1/parent/offerings?parentId=3
```
*`parentId`* is optional. When provided, session times appear in that parent's timezone.

- Response **`200`**:
```bash
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseTitle": "Minecraft Coding",
      "title": "Saturday Batch",
      "status": "ACTIVE",
      "sessions": [
        {
          "startTimeLocal": "2025-06-08T04:30:00+05:30",
          "endTimeLocal":   "2025-06-08T05:30:00+05:30",
          "startTimeUtc":   "2025-06-07T23:00:00Z",
          "timezone": "Asia/Kolkata"
        }
      ]
    }
  ]
}
```

### Book an Offering

```bash
POST /api/v1/parent/bookings
Content-Type: application/json

{
  "parentId":   3,
  "offeringId": 1
}
```

- Response **`201`**:
```bash
json{
  "success": true,
  "data": {
    "id": 1,
    "offeringTitle": "Saturday Batch",
    "courseTitle": "Minecraft Coding",
    "status": "CONFIRMED",
    "sessions": [...]
  }
}
```
- Conflict response **`409`**:
```bash
{
  "success": false,
  "error": "Time conflict with already-booked sessions: Session on 2025-06-07 (offering id: 2)"
}
```

### Get Parent's Bookings
```bash
GET /api/v1/parent/{parentId}/bookings
```
Returns all confirmed bookings with sessions in the parent's timezone.

<h2><a class="anchor" id="6-error-responses"></a>6. Error Responses</h2>


All errors follow the same shape:
```text

HTTP Status                                          When
400                              Invalid input, wrong role, bad session times,sessions on offering
404                              User / Offering / Course not found
409                              Time conflict with booked sessions, duplicate booking,concurrent race    
500                              Unexpected server error
```
Response:
```bash
{
  "success": false,
  "error": "Human-readable explanation here",
  "timestamp": "2025-06-07T23:05:00"
}

```


The project follows a layered architecture where controllers handle API requests,
services contain business logic, repositories interact with database, and
DTOs are used for clean request and response models.


<h2><a class="anchor" id="7-author-and-contact"></a>7. Author & Contact</h2>

**Surekha Endla**
- Email: surekhaendla@gmail.com
- LinkedIn: https://www.linkedin.com/in/surekhaendla/
