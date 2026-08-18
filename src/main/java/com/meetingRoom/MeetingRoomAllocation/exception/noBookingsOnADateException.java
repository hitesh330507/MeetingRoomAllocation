package com.meetingRoom.MeetingRoomAllocation.exception;

import java.time.LocalDate;

public class noBookingsOnADateException extends RuntimeException{
    public noBookingsOnADateException(LocalDate date){
        super("No Bookings At the date :"+date);
    }
}
