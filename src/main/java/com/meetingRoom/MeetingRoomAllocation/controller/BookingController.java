package com.meetingRoom.MeetingRoomAllocation.controller;

import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.UpdateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.BookingResponse;
import com.meetingRoom.MeetingRoomAllocation.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping
    public List<BookingResponse> searchBookings(
            @RequestParam(required = false) Integer bookingId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookingService.searchBookings(bookingId, roomId, companyName, date);
    }

    @PutMapping("/{id}")
    public BookingResponse updateBooking(@PathVariable int id, @Valid @RequestBody UpdateBookingRequest request) {
        return bookingService.updateBooking(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable int id) {
        bookingService.cancelBooking(id);
    }
}
