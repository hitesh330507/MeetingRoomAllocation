package com.meetingRoom.MeetingRoomAllocation.mapper;

import com.meetingRoom.MeetingRoomAllocation.domain.Booking;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.UpdateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.BookingResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public Booking toDomain(CreateBookingRequest request) {
        return new Booking(0, request.companyName(), request.roomId(), request.startTime(), request.endTime(), request.meetingTitle(), request.reqCapacity());
    }

    public Booking toDomain(int id, UpdateBookingRequest request) {
        return new Booking(id, request.companyName(), request.roomId(), request.startTime(), request.endTime(), request.meetingTitle(), request.reqCapacity());
    }

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getCompanyName(), booking.getRoomId(), booking.getStartTime(), booking.getEndTime(), booking.getMeetingTitle(), booking.getReqCapacity());
    }
}
