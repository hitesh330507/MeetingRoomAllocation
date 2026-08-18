package com.meetingRoom.MeetingRoomAllocation.repository;

import com.meetingRoom.MeetingRoomAllocation.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(int bookingId);
    void delete(int bookingId);
    List<Booking> findAll();
    List<Booking> findByRoomId(int roomId);
    List<Booking> findByCompanyName(String companyName);
}
