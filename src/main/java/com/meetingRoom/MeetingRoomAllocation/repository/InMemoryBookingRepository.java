package com.meetingRoom.MeetingRoomAllocation.repository;

import com.meetingRoom.MeetingRoomAllocation.domain.Booking;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentHashMap<Integer, Booking> bookingsById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<Integer>> bookingIdsByRoomId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Integer>> bookingIdsByCompanyName = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public Booking save(Booking booking) {
        int id = booking.getId() == 0 ? sequence.getAndIncrement() : booking.getId();
        Booking saved = new Booking(id, booking.getCompanyName(), booking.getRoomId(), booking.getStartTime(), booking.getEndTime(), booking.getMeetingTitle(), booking.getReqCapacity());
        Booking existing = bookingsById.put(id, saved);

        if (existing != null) {
            if (existing.getRoomId() != saved.getRoomId()) {
                removeBookingIdFromRoom(existing.getRoomId(), id);
            }
            if (!existing.getCompanyName().equalsIgnoreCase(saved.getCompanyName())) {
                removeBookingIdFromCompany(existing.getCompanyName(), id);
            }
        }

        bookingIdsByRoomId.computeIfAbsent(saved.getRoomId(), key -> ConcurrentHashMap.newKeySet()).add(id);
        bookingIdsByCompanyName.computeIfAbsent(saved.getCompanyName().toLowerCase(), key -> ConcurrentHashMap.newKeySet()).add(id);
        return saved;
    }

    @Override
    public Optional<Booking> findById(int bookingId) {
        return Optional.ofNullable(bookingsById.get(bookingId));
    }

    @Override
    public void delete(int bookingId) {
        Booking removed = bookingsById.remove(bookingId);
        if (removed != null) {
            removeBookingIdFromRoom(removed.getRoomId(), bookingId);
            removeBookingIdFromCompany(removed.getCompanyName(), bookingId);
        }
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookingsById.values());
    }

    @Override
    public List<Booking> findByRoomId(int roomId) {
        Set<Integer> ids = bookingIdsByRoomId.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
        return ids.stream()
                .map(bookingsById::get)
                .filter(booking -> booking != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByCompanyName(String companyName) {
        Set<Integer> ids = bookingIdsByCompanyName.getOrDefault(companyName.toLowerCase(), ConcurrentHashMap.newKeySet());
        return ids.stream()
                .map(bookingsById::get)
                .filter(booking -> booking != null)
                .collect(Collectors.toList());
    }

    private void removeBookingIdFromRoom(int roomId, int bookingId) {
        Set<Integer> ids = bookingIdsByRoomId.get(roomId);
        if (ids != null) {
            ids.remove(bookingId);
        }
    }

    private void removeBookingIdFromCompany(String companyName, int bookingId) {
        Set<Integer> ids = bookingIdsByCompanyName.get(companyName.toLowerCase());
        if (ids != null) {
            ids.remove(bookingId);
        }
    }
}
