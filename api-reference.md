# API Reference — Global Class Booking System

Base URL: `http://localhost:8080`

All responses follow this envelope:

```json
{ "success": true,  "data": { },  "timestamp": "2026-05-30T16:04:15" }
{ "success": false, "error": "...", "timestamp": "2026-05-30T16:04:15" }
```

---

## Teacher APIs

### 1. Create Offering

Creates a new schedulable batch/section for a course.

```
POST /api/v1/teacher/offerings
Content-Type: application/json
```

**Request body**

```json
{
  "teacherId":   1,
  "courseId":    1,
  "title":       "Saturday Batch",
  "description": "8-week Minecraft coding every Saturday",
  "maxCapacity": 20
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| teacherId | Long | Yes | Must be a user with role TEACHER |
| courseId | Long | Yes | Must exist in courses table |
| title | String | Yes | Name of the batch/section |
| description | String | No | Optional description |
| maxCapacity | Integer | No | Defaults to 30 |

**Response 201**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "courseId": 1,
    "courseTitle": "Minecraft Coding",
    "teacherId": 1,
    "teacherName": "Alice Teacher",
    "teacherTimezone": "America/New_York",
    "title": "Saturday Batch",
    "description": "8-week Minecraft coding every Saturday",
    "maxCapacity": 20,
    "status": "ACTIVE",
    "totalSessions": 0,
    "createdAt": "2026-05-30T15:38:26",
    "sessions": []
  },
  "timestamp": "2026-05-30T15:38:26"
}
```

**Error responses**

| Status | Condition | Message |
|--------|-----------|---------|
| 400 | teacherId is a PARENT | "User 1 is not a TEACHER." |
| 404 | teacherId not found | "User not found with id: 1" |
| 404 | courseId not found | "Course not found with id: 1" |

---

### 2. Add Session to Offering

Adds a single meeting slot to an offering. Submit time in your local timezone using ISO-8601 format with the UTC offset.

```
POST /api/v1/teacher/offerings/{offeringId}/sessions
Content-Type: application/json
```

**Path parameter**

| Parameter | Type | Description |
|-----------|------|-------------|
| offeringId | Long | ID of the offering to add the session to |

**Request body**

```json
{
  "startTime": "2025-06-07T18:00:00-05:00",
  "endTime":   "2025-06-07T19:00:00-05:00"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| startTime | ZonedDateTime (ISO-8601) | Yes | Include UTC offset: -05:00, +05:30, Z |
| endTime | ZonedDateTime (ISO-8601) | Yes | Must be after startTime |

**Timezone format examples**

| Teacher location | Format example |
|----------------|----------------|
| New York (EST) | 2025-06-07T18:00:00-05:00 |
| London (GMT+1) | 2025-06-07T18:00:00+01:00 |
| India (IST) | 2025-06-07T18:00:00+05:30 |
| UTC | 2025-06-07T18:00:00Z |

**Response 201**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "offeringId": 1,
    "teacherId": 1,
    "startTimeLocal": "2025-06-07T18:00:00-05:00",
    "endTimeLocal":   "2025-06-07T19:00:00-05:00",
    "startTimeUtc":   "2025-06-07T23:00:00Z",
    "endTimeUtc":     "2025-06-08T00:00:00Z",
    "timezone": "America/New_York"
  },
  "timestamp": "2026-05-30T15:38:26"
}
```

Note: `startTimeLocal` shows the time in the teacher's registered timezone for verification.
`startTimeUtc` shows what was stored in the database.

**Error responses**

| Status | Condition | Message |
|--------|-----------|---------|
| 400 | endTime before startTime | "endTime must be after startTime." |
| 400 | startTime in the past | "Cannot add a session in the past." |
| 400 | Offering is CANCELLED | "Cannot add sessions to a CANCELLED offering." |
| 404 | offeringId not found | "Offering not found with id: 1" |

---

### 3. Get Teacher's Offerings

Returns all offerings created by a teacher, including their sessions. Session times shown in the teacher's registered timezone.

```
GET /api/v1/teacher/{teacherId}/offerings
```

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseTitle": "Minecraft Coding",
      "teacherName": "Alice Teacher",
      "teacherTimezone": "America/New_York",
      "title": "Saturday Batch",
      "status": "ACTIVE",
      "totalSessions": 3,
      "sessions": [
        {
          "id": 1,
          "startTimeLocal": "2025-06-07T18:00:00-05:00",
          "endTimeLocal":   "2025-06-07T19:00:00-05:00",
          "startTimeUtc":   "2025-06-07T23:00:00Z",
          "endTimeUtc":     "2025-06-08T00:00:00Z",
          "timezone": "America/New_York"
        }
      ]
    }
  ],
  "timestamp": "2026-05-30T15:38:26"
}
```

---

## Parent APIs

### 4. Browse Available Offerings

Returns all ACTIVE offerings. Pass `parentId` to see session times in the parent's registered timezone.

```
GET /api/v1/parent/offerings
GET /api/v1/parent/offerings?parentId=3
```

**Query parameter**

| Parameter | Type | Required | Notes |
|-----------|------|----------|-------|
| parentId | Long | No | When provided, times shown in parent's timezone. When omitted, times shown in UTC. |

**Response 200** (with parentId=3, Carol in Asia/Kolkata)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseTitle": "Minecraft Coding",
      "teacherName": "Alice Teacher",
      "teacherTimezone": "America/New_York",
      "title": "Saturday Batch",
      "status": "ACTIVE",
      "totalSessions": 3,
      "sessions": [
        {
          "startTimeLocal": "2025-06-08T04:30:00+05:30",
          "endTimeLocal":   "2025-06-08T05:30:00+05:30",
          "startTimeUtc":   "2025-06-07T23:00:00Z",
          "endTimeUtc":     "2025-06-08T00:00:00Z",
          "timezone": "Asia/Kolkata"
        }
      ]
    }
  ],
  "timestamp": "2026-05-30T15:38:26"
}
```

---

### 5. Book an Offering

Books an entire offering for a parent (all sessions together). Runs conflict detection against all of the parent's existing bookings.

```
POST /api/v1/parent/bookings
Content-Type: application/json
```

**Request body**

```json
{
  "parentId":   3,
  "offeringId": 1
}
```

| Field | Type | Required |
|-------|------|----------|
| parentId | Long | Yes |
| offeringId | Long | Yes |

**Response 201**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "offeringId": 1,
    "offeringTitle": "Saturday Batch",
    "courseTitle": "Minecraft Coding",
    "teacherId": 1,
    "teacherName": "Alice Teacher",
    "parentId": 3,
    "status": "CONFIRMED",
    "bookedAt": "2026-05-30T16:04:15",
    "sessions": [
      {
        "startTimeLocal": "2025-06-08T04:30:00+05:30",
        "endTimeLocal":   "2025-06-08T05:30:00+05:30",
        "startTimeUtc":   "2025-06-07T23:00:00Z",
        "timezone": "Asia/Kolkata"
      }
    ]
  },
  "timestamp": "2026-05-30T16:04:15"
}
```

**Error responses**

| Status | Condition | Message |
|--------|-----------|---------|
| 400 | parentId is a TEACHER | "User 1 is not a PARENT." |
| 400 | Already booked this offering | "You have already booked offering 'Saturday Batch'." |
| 400 | Offering has no sessions | "Offering has no sessions yet." |
| 400 | Offering is not ACTIVE | "Offering is CANCELLED and cannot be booked." |
| 404 | parentId not found | "User not found with id: 3" |
| 404 | offeringId not found | "Offering not found with id: 1" |
| 409 | Schedule overlap with existing booking | "Cannot book 'Roblox Design'. Time conflict with already-booked sessions: Session on 2025-06-14 (offering id: 1)" |
| 409 | Concurrent duplicate booking (race condition) | "This offering has already been booked by you, or a concurrent request was processed first." |

---

### 6. Get Parent's Bookings

Returns all confirmed bookings for a parent. Session times shown in the parent's registered timezone.

```
GET /api/v1/parent/{parentId}/bookings
```

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "offeringTitle": "Saturday Batch",
      "courseTitle": "Minecraft Coding",
      "teacherName": "Alice Teacher",
      "status": "CONFIRMED",
      "bookedAt": "2026-05-30T16:04:15",
      "sessions": [
        {
          "startTimeLocal": "2025-06-08T04:30:00+05:30",
          "endTimeLocal":   "2025-06-08T05:30:00+05:30",
          "startTimeUtc":   "2025-06-07T23:00:00Z",
          "timezone": "Asia/Kolkata"
        }
      ]
    }
  ],
  "timestamp": "2026-05-30T16:04:15"
}
```

---

## Error Reference

| HTTP Status | Exception | When it happens |
|-------------|-----------|-----------------|
| 400 | BusinessException | Wrong role, invalid times, no sessions on offering |
| 400 | MethodArgumentNotValidException | Missing required fields, constraint violations |
| 404 | ResourceNotFoundException | User / Offering / Course ID does not exist |
| 409 | TimeConflictException | Session times overlap with existing booking |
| 409 | DataIntegrityViolationException | DB unique constraint — concurrent duplicate |
| 500 | Exception | Unexpected server error |

---

## Postman Collection

### Recommended test order

Run in this sequence to build up data state correctly.

```
Step 1:  POST /teacher/offerings                  create Offering 1 (Alice, Minecraft)
Step 2:  POST /teacher/offerings                  create Offering 2 (Bob, Python)
Step 3:  POST /teacher/offerings/1/sessions        add session June 7
Step 4:  POST /teacher/offerings/1/sessions       add session June 14
Step 5:  POST /teacher/offerings/1/sessions       add session June 21
Step 6:  POST /teacher/offerings/2/sessions       add overlapping session June 14
Step 7:  GET  /teacher/1/offerings                verify Alice's sessions in NY time
Step 8:  GET  /parent/offerings?parentId=3        Carol sees IST times
Step 9:  POST /parent/bookings                    Carol books Offering 1 -> 201
Step 10: POST /parent/bookings                    Carol tries Offering 2 -> 409 conflict
Step 11: POST /parent/bookings                    Carol books Offering 1 again -> 400 duplicate
Step 12: GET  /parent/3/bookings                  Carol's bookings in IST
```
