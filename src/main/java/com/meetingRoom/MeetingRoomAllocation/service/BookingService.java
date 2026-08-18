package com.meetingRoom.MeetingRoomAllocation.service;

import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.UpdateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.BookingResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    List<BookingResponse> searchBookings(Integer bookingId, Integer roomId, String companyName, LocalDate date);
    BookingResponse updateBooking(int bookingId, UpdateBookingRequest request);
    void cancelBooking(int bookingId);
}
