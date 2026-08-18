# 🏢 Meeting Room Allocation System

A high-performance, thread-safe Spring Boot REST API for managing meeting rooms and scheduling reservations in a multi-tenant corporate environment.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture & Design Highlights](#-architecture--design-highlights)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Build and Run](#build-and-run)
  - [Running Tests](#running-tests)
- [API Documentation & Swagger UI](#-api-documentation--swagger-ui)
- [API Reference & Examples](#-api-reference--examples)
  - [Rooms API](#1-rooms-api)
  - [Bookings API](#2-bookings-api)
- [Business Rules & Validation](#-business-rules--validation)
- [Concurrency & Thread Safety](#-concurrency--thread-safety)
- [Error Handling](#-error-handling)

---

## 🌟 Overview

The **Meeting Room Allocation System** is designed to streamline conference room management. It provides APIs to register rooms with specific facilities and capacities, check availability, book meeting slots without overlaps, search reservations with flexible filtering, update existing bookings, and release allocations when cancelled.

---

## ✨ Key Features

- **Room Management**: Register meeting rooms with floor location, seating capacity, and supported facilities (TV, Projector, Video Conferencing).
- **Room Discovery & Filtering**: Search available rooms filtered by minimum capacity requirement and specific facility.
- **Conflict-Free Booking**: Schedule meetings with strict validation preventing double-booking or slot overlapping.
- **Adjacent / Back-to-Back Bookings**: Supports back-to-back reservations (e.g., slot `09:00 - 10:00` followed by `10:00 - 11:00`).
- **Flexible Reservation Search**: Query bookings by `bookingId`, `roomId`, `companyName`, and `date`, with chronologically sorted results.
- **Booking Modification & Cancellation**: Reschedule meetings across timeslots or rooms, and cancel bookings to free up capacity.
- **Thread-Safe & Concurrency Guarded**: In-memory repository with fine-grained per-room locking mechanisms and deadlock-free multi-room lock acquisition.

---

## 🏗 Architecture & Design Highlights

- **Layered Architecture**: Clear separation of concerns across Controller, Service, Repository, DTO, Mapper, and Domain layers.
- **In-Memory Thread-Safe Data Store**: Built with `ConcurrentHashMap`, secondary indexing for fast lookups by company name and room ID, and atomic sequence generators.
- **Fine-Grained Concurrency Control**: Uses `ReentrantLock` keyed per room to serialize booking operations on the same room while allowing concurrent bookings across different rooms.
- **Deadlock Prevention**: Ordered multi-room locking during room-switch updates (`firstRoomId < secondRoomId`).

---

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.x (Web MVC, Validation)
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Boilerplate Reduction**: Project Lombok
- **Testing**: JUnit 5, Spring Boot Starter Test
- **Build Tool**: Apache Maven (Maven Wrapper included)

---

## 📁 Project Structure

```text
MeetingRoomAllocation/
├── pom.xml                               # Maven project configuration
├── HELP.md                               # Spring Boot helper documentation
├── mvnw / mvnw.cmd                       # Maven wrapper executables
└── src/
    ├── main/
    │   ├── java/com/meetingRoom/MeetingRoomAllocation/
    │   │   ├── MeetingRoomAllocationApplication.java  # Application entry point
    │   │   ├── controller/               # REST API Controllers
    │   │   │   ├── BookingController.java
    │   │   │   └── RoomController.java
    │   │   ├── domain/                   # Core domain entities & enums
    │   │   │   ├── Booking.java
    │   │   │   ├── Facility.java
    │   │   │   └── Room.java
    │   │   ├── dto/                      # Data Transfer Objects (Records)
    │   │   │   ├── request/
    │   │   │   │   ├── CreateBookingRequest.java
    │   │   │   │   ├── CreateRoomRequest.java
    │   │   │   │   └── UpdateBookingRequest.java
    │   │   │   └── response/
    │   │   │       ├── BookingResponse.java
    │   │   │       └── RoomResponse.java
    │   │   ├── exception/                # Domain-specific custom exceptions
    │   │   │   ├── BookingConflictException.java
    │   │   │   ├── BookingNotFoundException.java
    │   │   │   ├── CompanyNotFoundException.java
    │   │   │   ├── InvalidBookingRequestException.java
    │   │   │   ├── InvalidBookingTimeException.java
    │   │   │   ├── InvalidCapacityException.java
    │   │   │   ├── RoomCapacityException.java
    │   │   │   ├── RoomNotFoundException.java
    │   │   │   └── noBookingsOnADateException.java
    │   │   ├── mapper/                   # Entity-DTO mapping components
    │   │   │   ├── BookingMapper.java
    │   │   │   └── RoomMapper.java
    │   │   ├── repository/               # Repository interfaces & In-Memory stores
    │   │   │   ├── BookingRepository.java
    │   │   │   ├── InMemoryBookingRepository.java
    │   │   │   ├── InMemoryRoomRepository.java
    │   │   │   └── RoomRepository.java
    │   │   └── service/                  # Business logic interfaces & services
    │   │       ├── BookingService.java
    │   │       ├── RoomService.java
    │   │       └── impl/
    │   │           ├── BookingServiceImpl.java
    │   │           └── RoomServiceImpl.java
    │   └── resources/
    │       ├── application.properties    # Application settings
    │       └── banner.txt                # Custom startup banner
    └── test/
        └── java/com/meetingRoom/MeetingRoomAllocation/
            ├── MeetingRoomAllocationApplicationTests.java
            └── service/
                └── BookingServiceImplTest.java # Comprehensive unit & concurrency tests
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher
- **Maven**: 3.9+ (or use the bundled `./mvnw`)

### Build and Run

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd MeetingRoomAllocation
   ```

2. **Build the application:**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

The application starts by default on port `8080` (accessible at `http://localhost:8080`).

### Running Tests

Execute test suites including unit tests and concurrent allocation race-condition tests:

```bash
./mvnw test
```

---

## 📖 API Documentation & Swagger UI

Once the application is running, explore and test the interactive OpenAPI documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📡 API Reference & Examples

### 1. Rooms API

#### **Create a Room**
- **Endpoint**: `POST /rooms`
- **Status**: `201 Created`
- **Request Body**:
  ```json
  {
    "roomNumber": "Conference Room A",
    "floor": 2,
    "capacity": 12,
    "facilities": ["PROJECTOR", "VIDEO_CONFERENCING"]
  }
  ```
- **Response**:
  ```json
  {
    "id": 1,
    "roomNumber": "Conference Room A",
    "floor": 2,
    "capacity": 12,
    "facilities": ["PROJECTOR", "VIDEO_CONFERENCING"]
  }
  ```
- **cURL Example**:
  ```bash
  curl -X POST http://localhost:8080/rooms \
    -H "Content-Type: application/json" \
    -d '{
      "roomNumber": "Room 101",
      "floor": 1,
      "capacity": 10,
      "facilities": ["PROJECTOR", "TV"]
    }'
  ```

#### **List / Filter Rooms**
- **Endpoint**: `GET /rooms`
- **Query Parameters**:
  - `capacity` *(optional, Integer)*: Minimum required seating capacity.
  - `facility` *(optional, String)*: Required facility (`TV`, `PROJECTOR`, `VIDEO_CONFERENCING`).
- **Response**:
  ```json
  [
    {
      "id": 1,
      "roomNumber": "Room 101",
      "floor": 1,
      "capacity": 10,
      "facilities": ["PROJECTOR", "TV"]
    }
  ]
  ```
- **cURL Example**:
  ```bash
  curl "http://localhost:8080/rooms?capacity=8&facility=PROJECTOR"
  ```

---

### 2. Bookings API

#### **Create a Booking**
- **Endpoint**: `POST /bookings`
- **Status**: `201 Created`
- **Request Body**:
  ```json
  {
    "companyName": "Acme Corp",
    "roomId": 1,
    "startTime": "2026-09-01T10:00:00",
    "endTime": "2026-09-01T11:30:00",
    "meetingTitle": "Q3 Planning Sync",
    "reqCapacity": 8
  }
  ```
- **Response**:
  ```json
  {
    "id": 1,
    "companyName": "Acme Corp",
    "roomId": 1,
    "startTime": "2026-09-01T10:00:00",
    "endTime": "2026-09-01T11:30:00",
    "meetingTitle": "Q3 Planning Sync",
    "reqCapacity": 8
  }
  ```
- **cURL Example**:
  ```bash
  curl -X POST http://localhost:8080/bookings \
    -H "Content-Type: application/json" \
    -d '{
      "companyName": "Acme Corp",
      "roomId": 1,
      "startTime": "2026-09-01T10:00:00",
      "endTime": "2026-09-01T11:30:00",
      "meetingTitle": "Q3 Planning Sync",
      "reqCapacity": 8
    }'
  ```

#### **Search Bookings**
- **Endpoint**: `GET /bookings`
- **Query Parameters** (any combination supported):
  - `bookingId` *(optional, Integer)*: Filter by exact booking ID.
  - `roomId` *(optional, Integer)*: Filter by room ID.
  - `companyName` *(optional, String)*: Filter by company name.
  - `date` *(optional, ISO Date `YYYY-MM-DD`)*: Filter by date of booking.
- **Response**: List of matching bookings sorted ascending by `startTime`.
  ```json
  [
    {
      "id": 1,
      "companyName": "Acme Corp",
      "roomId": 1,
      "startTime": "2026-09-01T10:00:00",
      "endTime": "2026-09-01T11:30:00",
      "meetingTitle": "Q3 Planning Sync",
      "reqCapacity": 8
    }
  ]
  ```
- **cURL Example**:
  ```bash
  curl "http://localhost:8080/bookings?companyName=Acme%20Corp&date=2026-09-01"
  ```

#### **Update a Booking**
- **Endpoint**: `PUT /bookings/{id}`
- **Status**: `200 OK`
- **Request Body**:
  ```json
  {
    "companyName": "Acme Corp",
    "roomId": 1,
    "startTime": "2026-09-01T11:30:00",
    "endTime": "2026-09-01T12:30:00",
    "meetingTitle": "Rescheduled Q3 Sync",
    "reqCapacity": 8
  }
  ```
- **Response**: Updated `BookingResponse`.
- **cURL Example**:
  ```bash
  curl -X PUT http://localhost:8080/bookings/1 \
    -H "Content-Type: application/json" \
    -d '{
      "companyName": "Acme Corp",
      "roomId": 1,
      "startTime": "2026-09-01T11:30:00",
      "endTime": "2026-09-01T12:30:00",
      "meetingTitle": "Rescheduled Q3 Sync",
      "reqCapacity": 8
    }'
  ```

#### **Cancel a Booking**
- **Endpoint**: `DELETE /bookings/{id}`
- **Status**: `204 No Content`
- **cURL Example**:
  ```bash
  curl -X DELETE http://localhost:8080/bookings/1
  ```

---

## 🔒 Business Rules & Validation

| Rule | Description | Exception |
|---|---|---|
| **Future Dates Only** | Meeting `startTime` must be strictly in the future. | `InvalidBookingTimeException` |
| **Chronological Times** | `endTime` must be strictly after `startTime`. | `InvalidBookingTimeException` |
| **Room Capacity** | Requested capacity (`reqCapacity`) cannot be negative and cannot exceed the room's maximum capacity. | `InvalidCapacityException` / `RoomCapacityException` |
| **No Overlaps** | A booking cannot overlap with existing bookings for the same room ($start_A < end_B$ and $start_B < end_A$). | `BookingConflictException` |
| **Adjacent Slots Allowed** | End time matching start time of another booking is valid (no gap required). | Allowed |
| **Mandatory Metadata** | Company name and meeting title must not be blank. Room ID must be positive. | Validation / `InvalidBookingTimeException` |

---

## ⚡ Concurrency & Thread Safety

- **Room-Level Locks**: Every room is protected by a dedicated `ReentrantLock` stored in a thread-safe `ConcurrentHashMap`. Multiple requests attempting to book the *same room* are synchronized to prevent double booking.
- **Deadlock-Free Updates**: When updating a booking to switch rooms, locks for both the source and target rooms are acquired in deterministic order (`min(roomId1, roomId2)` followed by `max(roomId1, roomId2)`).
- **Concurrent Reads**: Read queries utilize non-blocking concurrent maps for high-throughput listing and filtering.

---

## 🛑 Error Handling

Custom domain exceptions map to distinct failure scenarios:

- `RoomNotFoundException`: Room ID does not exist.
- `BookingNotFoundException`: Booking ID not found when querying, updating, or deleting.
- `BookingConflictException`: Requested time slot overlaps with another active meeting.
- `RoomCapacityException`: Requested capacity exceeds room size.
- `InvalidCapacityException`: Negative requested capacity supplied.
- `InvalidBookingTimeException`: Times are invalid or set in the past.
