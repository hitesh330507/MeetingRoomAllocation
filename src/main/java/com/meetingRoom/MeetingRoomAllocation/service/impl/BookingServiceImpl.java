package com.meetingRoom.MeetingRoomAllocation.service.impl;

import com.meetingRoom.MeetingRoomAllocation.domain.Booking;
import com.meetingRoom.MeetingRoomAllocation.domain.Room;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.UpdateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.BookingResponse;
import com.meetingRoom.MeetingRoomAllocation.exception.BookingConflictException;
import com.meetingRoom.MeetingRoomAllocation.exception.BookingNotFoundException;
import com.meetingRoom.MeetingRoomAllocation.exception.InvalidBookingTimeException;
import com.meetingRoom.MeetingRoomAllocation.exception.InvalidCapacityException;
import com.meetingRoom.MeetingRoomAllocation.exception.RoomCapacityException;
import com.meetingRoom.MeetingRoomAllocation.exception.RoomNotFoundException;
import com.meetingRoom.MeetingRoomAllocation.mapper.BookingMapper;
import com.meetingRoom.MeetingRoomAllocation.repository.BookingRepository;
import com.meetingRoom.MeetingRoomAllocation.repository.RoomRepository;
import com.meetingRoom.MeetingRoomAllocation.service.BookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final Map<Integer, Lock> roomLocks = new ConcurrentHashMap<>();

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        validateBookingRequest(request.companyName(), request.roomId(), request.startTime(), request.endTime(), request.meetingTitle(), request.reqCapacity());
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new RoomNotFoundException(request.roomId()));
        validateRoomCapacity(request.reqCapacity(), room);

        Lock lock = roomLocks.computeIfAbsent(room.getId(), id -> new ReentrantLock());
        lock.lock();
        try {
            checkBookingConflict(room.getId(), request.startTime(), request.endTime(), 0);
            Booking booking = bookingMapper.toDomain(request);
            Booking saved = bookingRepository.save(booking);
            return bookingMapper.toResponse(saved);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<BookingResponse> searchBookings(Integer bookingId, Integer roomId, String companyName, LocalDate date) {
        List<Booking> candidates;

        if (bookingId != null) {
            candidates = bookingRepository.findById(bookingId)
                    .map(List::of)
                    .orElseThrow(()-> new  BookingNotFoundException(bookingId));
        } else {
            candidates = new ArrayList<>(bookingRepository.findAll());
        }

        if (roomId != null) {
            roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
            if (bookingId != null) {
                candidates = intersect(candidates, bookingRepository.findByRoomId(roomId));
            } else {
                candidates = bookingRepository.findByRoomId(roomId);
            }
        }

        if (companyName != null) {
            List<Booking> byCompany = bookingRepository.findByCompanyName(companyName);
            candidates = intersect(candidates, byCompany);
        }

        if (date != null) {
            candidates = candidates.stream()
                    .filter(booking -> isSameDate(booking.getStartTime(), date) || isSameDate(booking.getEndTime(), date))
                    .collect(Collectors.toList());
        }

        return candidates.stream()
                .sorted(Comparator.comparing(Booking::getStartTime))
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse updateBooking(int bookingId, UpdateBookingRequest request) {
        Booking existing = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        validateBookingRequest(request.companyName(), request.roomId(), request.startTime(), request.endTime(), request.meetingTitle(), request.reqCapacity());
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new RoomNotFoundException(request.roomId()));
        validateRoomCapacity(request.reqCapacity(), room);

        List<Integer> roomIds = getOrderedRoomIds(existing.getRoomId(), request.roomId());
        Lock firstLock = roomLocks.computeIfAbsent(roomIds.get(0), id -> new ReentrantLock());
        Lock secondLock = roomIds.size() == 2 ? roomLocks.computeIfAbsent(roomIds.get(1), id -> new ReentrantLock()) : firstLock;

        firstLock.lock();
        if (secondLock != firstLock) {
            secondLock.lock();
        }
        try {
            checkBookingConflict(request.roomId(), request.startTime(), request.endTime(), bookingId);
            Booking updated = bookingMapper.toDomain(bookingId, request);
            Booking saved = bookingRepository.save(updated);
            return bookingMapper.toResponse(saved);
        } finally {
            if (secondLock != firstLock) {
                secondLock.unlock();
            }
            firstLock.unlock();
        }
    }

    @Override
    public void cancelBooking(int bookingId) {
        Booking existing = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
        Lock lock = roomLocks.computeIfAbsent(existing.getRoomId(), id -> new ReentrantLock());
        lock.lock();
        try {
            bookingRepository.delete(bookingId);
        } finally {
            lock.unlock();
        }
    }

    private void validateBookingRequest(String companyName, int roomId, LocalDateTime startTime, LocalDateTime endTime, String meetingTitle, int reqCapacity) {
        if (startTime == null || endTime == null) {
            throw new InvalidBookingTimeException("Start time and end time must be provided");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidBookingTimeException("End time must be after start time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingTimeException("Booking cannot start in the past");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new InvalidBookingTimeException("Company name is required");
        }
        if (meetingTitle == null || meetingTitle.isBlank()) {
            throw new InvalidBookingTimeException("Meeting title is required");
        }
        if (roomId <= 0) {
            throw new InvalidBookingTimeException("Room id must be positive");
        }
        if(reqCapacity<0) throw new InvalidCapacityException("Capacity req Can't be negative");
    }

    private void validateRoomCapacity(int reqCapacity, Room room) {
        if (room.getCapacity() < reqCapacity) {
            throw new RoomCapacityException(reqCapacity, room.getCapacity());
        }
    }

    private void checkBookingConflict(int roomId, LocalDateTime startTime, LocalDateTime endTime, int ignoreBookingId) {
        List<Booking> bookings = bookingRepository.findByRoomId(roomId);
        for (Booking booking : bookings) {
            if (booking.getId() == ignoreBookingId) {
                continue;
            }
            if (overlaps(startTime, endTime, booking.getStartTime(), booking.getEndTime())) {
                throw new BookingConflictException("Booking conflicts with existing booking " + booking.getId());
            }
        }
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private boolean isSameDate(LocalDateTime dateTime, LocalDate date) {
        return dateTime.toLocalDate().equals(date);
    }

    private List<Booking> intersect(List<Booking> first, List<Booking> second) {
        return first.stream()
                .filter(second::contains)
                .collect(Collectors.toList());
    }

    private List<Integer> getOrderedRoomIds(int firstRoomId, int secondRoomId) {
        if (firstRoomId == secondRoomId) {
            return List.of(firstRoomId);
        }
        return firstRoomId < secondRoomId ? List.of(firstRoomId, secondRoomId) : List.of(secondRoomId, firstRoomId);
    }
}
