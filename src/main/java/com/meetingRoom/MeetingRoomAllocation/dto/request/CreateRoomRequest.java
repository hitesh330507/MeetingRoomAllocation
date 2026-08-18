package com.meetingRoom.MeetingRoomAllocation.dto.request;

import com.meetingRoom.MeetingRoomAllocation.domain.Facility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record CreateRoomRequest(
        @NotBlank String roomNumber,
        @Min(0) int floor,
        @Min(1) int capacity,
        @NotEmpty Set<@NotNull Facility> facilities
) {
}
