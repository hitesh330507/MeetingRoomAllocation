package com.meetingRoom.MeetingRoomAllocation.exception;

public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
