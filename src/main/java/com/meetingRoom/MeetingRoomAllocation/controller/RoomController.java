package com.meetingRoom.MeetingRoomAllocation.controller;

import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateRoomRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.RoomResponse;
import com.meetingRoom.MeetingRoomAllocation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping
    public List<RoomResponse> getRooms(@RequestParam(required = false) Integer capacity,
                                       @RequestParam(required = false) String facility) {
        return roomService.listRooms(capacity, facility);
    }
}
