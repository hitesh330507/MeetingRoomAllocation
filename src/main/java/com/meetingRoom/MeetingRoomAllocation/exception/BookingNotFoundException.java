package com.meetingRoom.MeetingRoomAllocation.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(int bookingId) {
        super("Booking not found: " + bookingId);
    }
}
