package com.meetingRoom.MeetingRoomAllocation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
public class Booking {
    private final int id;
    private final String companyName;
    private final int roomId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String meetingTitle;
    private final int reqCapacity;
}
