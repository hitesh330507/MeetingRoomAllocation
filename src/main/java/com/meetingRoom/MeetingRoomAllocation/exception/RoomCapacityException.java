package com.meetingRoom.MeetingRoomAllocation.exception;

public class RoomCapacityException extends RuntimeException {
    public RoomCapacityException(int requested, int capacity) {
        super("Requested capacity " + requested + " exceeds room capacity " + capacity);
    }
}
