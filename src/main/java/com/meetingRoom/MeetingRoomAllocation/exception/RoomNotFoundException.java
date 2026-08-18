package com.meetingRoom.MeetingRoomAllocation.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(int roomId) {
        super("Room not found: " + roomId);
    }
}
