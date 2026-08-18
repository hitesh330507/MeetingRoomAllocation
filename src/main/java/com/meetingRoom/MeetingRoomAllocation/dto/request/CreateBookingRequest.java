package com.meetingRoom.MeetingRoomAllocation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
public record CreateBookingRequest(
        @NotBlank String companyName,
        @Positive int roomId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotBlank String meetingTitle,
        @Positive int reqCapacity
) {
}
