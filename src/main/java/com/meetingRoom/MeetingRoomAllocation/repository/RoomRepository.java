package com.meetingRoom.MeetingRoomAllocation.repository;

import com.meetingRoom.MeetingRoomAllocation.domain.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    Room save(Room room);
    Optional<Room> findById(int roomId);
    List<Room> findAll();
}
