package com.meetingRoom.MeetingRoomAllocation.mapper;

import com.meetingRoom.MeetingRoomAllocation.domain.Room;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateRoomRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.RoomResponse;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
    public Room toDomain(CreateRoomRequest request) {
        return new Room(0, request.roomNumber(), request.floor(), request.capacity(), request.facilities());
    }

    public RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getFloor(), room.getCapacity(), room.getFacilities());
    }
}
