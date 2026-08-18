package com.meetingRoom.MeetingRoomAllocation.repository;

import com.meetingRoom.MeetingRoomAllocation.domain.Room;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryRoomRepository implements RoomRepository {
    private final ConcurrentHashMap<Integer, Room> roomsById = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    public Room save(Room room) {
        int id = room.getId() == 0 ? sequence.getAndIncrement() : room.getId();
        Room saved = new Room(id, room.getRoomNumber(), room.getFloor(), room.getCapacity(), room.getFacilities());
        roomsById.put(id, saved);
        return saved;
    }

    @Override
    public Optional<Room> findById(int roomId) {
        return Optional.ofNullable(roomsById.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(roomsById.values());
    }
}
