package com.meetingRoom.MeetingRoomAllocation.exception;

public class CompanyNotFoundException extends RuntimeException{
    public CompanyNotFoundException(String companyName) {
        super("Company with name "+companyName+" doesn't have any bookings");
    }
}
