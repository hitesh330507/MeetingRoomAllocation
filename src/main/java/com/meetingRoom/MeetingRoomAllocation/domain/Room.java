package com.meetingRoom.MeetingRoomAllocation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
public class Room {
    private final int id;
    private final String roomNumber;
    private final int floor;
    private final int capacity;
    private final Set<Facility> facilities;
}
