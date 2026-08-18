package com.meetingRoom.MeetingRoomAllocation.dto.response;

import java.time.LocalDateTime;

public record BookingResponse(
        int id,
        String companyName,
        int roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String meetingTitle,
        int reqCapacity
) {
}
