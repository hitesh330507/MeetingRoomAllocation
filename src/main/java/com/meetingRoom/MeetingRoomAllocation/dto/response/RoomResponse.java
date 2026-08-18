package com.meetingRoom.MeetingRoomAllocation.dto.response;

import com.meetingRoom.MeetingRoomAllocation.domain.Facility;

import java.util.Set;

public record RoomResponse(
        int id,
        String roomNumber,
        int floor,
        int capacity,
        Set<Facility> facilities
) {
}
