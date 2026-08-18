package com.meetingRoom.MeetingRoomAllocation.service;

import com.meetingRoom.MeetingRoomAllocation.domain.Room;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateRoomRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    List<RoomResponse> listRooms(Integer capacity, String facility);
    Room findRoom(int roomId);
}
