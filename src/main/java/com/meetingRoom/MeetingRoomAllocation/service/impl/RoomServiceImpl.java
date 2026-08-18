package com.meetingRoom.MeetingRoomAllocation.service.impl;

import com.meetingRoom.MeetingRoomAllocation.domain.Facility;
import com.meetingRoom.MeetingRoomAllocation.domain.Room;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateRoomRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.RoomResponse;
import com.meetingRoom.MeetingRoomAllocation.exception.RoomNotFoundException;
import com.meetingRoom.MeetingRoomAllocation.repository.RoomRepository;
import com.meetingRoom.MeetingRoomAllocation.service.RoomService;
import com.meetingRoom.MeetingRoomAllocation.mapper.RoomMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = roomMapper.toDomain(request);
        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }

    @Override
    public List<RoomResponse> listRooms(Integer capacity, String facility) {
        Facility facilityEnum = null;
        if (facility != null) {
            try {
                facilityEnum = Facility.valueOf(facility.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return List.of();
            }
        }
        Facility finalFacilityEnum = facilityEnum;
        return roomRepository.findAll().stream()
                .filter(room -> capacity == null || room.getCapacity() >= capacity)
                .filter(room -> finalFacilityEnum == null || room.getFacilities().contains(finalFacilityEnum))
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Room findRoom(int roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }
}
