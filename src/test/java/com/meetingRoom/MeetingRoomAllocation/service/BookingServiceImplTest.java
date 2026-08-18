package com.meetingRoom.MeetingRoomAllocation.service;

import com.meetingRoom.MeetingRoomAllocation.domain.Facility;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.CreateRoomRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.request.UpdateBookingRequest;
import com.meetingRoom.MeetingRoomAllocation.dto.response.BookingResponse;
import com.meetingRoom.MeetingRoomAllocation.dto.response.RoomResponse;
import com.meetingRoom.MeetingRoomAllocation.exception.BookingConflictException;
import com.meetingRoom.MeetingRoomAllocation.exception.BookingNotFoundException;
import com.meetingRoom.MeetingRoomAllocation.exception.InvalidBookingTimeException;
import com.meetingRoom.MeetingRoomAllocation.exception.InvalidCapacityException;
import com.meetingRoom.MeetingRoomAllocation.exception.RoomCapacityException;
import com.meetingRoom.MeetingRoomAllocation.exception.RoomNotFoundException;
import com.meetingRoom.MeetingRoomAllocation.mapper.BookingMapper;
import com.meetingRoom.MeetingRoomAllocation.mapper.RoomMapper;
import com.meetingRoom.MeetingRoomAllocation.repository.BookingRepository;
import com.meetingRoom.MeetingRoomAllocation.repository.InMemoryBookingRepository;
import com.meetingRoom.MeetingRoomAllocation.repository.InMemoryRoomRepository;
import com.meetingRoom.MeetingRoomAllocation.repository.RoomRepository;
import com.meetingRoom.MeetingRoomAllocation.service.impl.BookingServiceImpl;
import com.meetingRoom.MeetingRoomAllocation.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceImplTest {
    private RoomService roomService;
    private BookingService bookingService;
    private RoomResponse room;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new InMemoryRoomRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        roomService = new RoomServiceImpl(roomRepository, new RoomMapper());
        bookingService = new BookingServiceImpl(
                bookingRepository,
                roomRepository,
                new BookingMapper()
        );

        room = createRoom();
        start = LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0)
        );
        end = start.plusHours(1);
    }

    private RoomResponse createRoom() {
        CreateRoomRequest roomRequest = new CreateRoomRequest("Room 101", 1, 10, Set.of(Facility.PROJECTOR));
        return roomService.createRoom(roomRequest);
    }

    private CreateBookingRequest createRequest(int roomId, LocalDateTime start, LocalDateTime end) {
        return createRequest(roomId, start, end, 8);
    }

    private CreateBookingRequest createRequest(int roomId, LocalDateTime start, LocalDateTime end, int reqCapacity) {
        return new CreateBookingRequest("Acme", roomId, start, end, "Team Sync", reqCapacity);
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        BookingResponse booking = bookingService.createBooking(createRequest(room.id(), start, end));

        assertNotNull(booking);
        assertEquals(room.id(), booking.roomId());
        assertEquals(start, booking.startTime());
        assertEquals(end, booking.endTime());
    }

    @Test
    void shouldRejectOverlappingBooking() {
        bookingService.createBooking(createRequest(room.id(), start, end));

        BookingConflictException exception = assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(createRequest(room.id(), start.plusMinutes(30), end.plusMinutes(30))));

        assertTrue(exception.getMessage().contains("conflicts"));
    }

    @Test
    void shouldAllowAdjacentBooking() {
        bookingService.createBooking(createRequest(room.id(), start, end));

        BookingResponse second = bookingService.createBooking(createRequest(room.id(), end, end.plusHours(1)));

        assertNotNull(second);
        assertEquals(end, second.startTime());
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        RoomResponse room = createRoom();
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        LocalDateTime end = start.minusHours(1);

        assertThrows(InvalidBookingTimeException.class,
                () -> bookingService.createBooking(createRequest(room.id(), start, end)));
    }

    @Test
    void shouldRejectUnknownRoom() {
        assertThrows(RoomNotFoundException.class,
                () -> bookingService.createBooking(createRequest(999, start, end)));
    }

    @Test
    void shouldRejectNegativeRequestedCapacity() {
        InvalidCapacityException exception = assertThrows(InvalidCapacityException.class,
                () -> bookingService.createBooking(createRequest(room.id(), start, end, -1)));

        assertEquals("Capacity req Can't be negative", exception.getMessage());
    }

    @Test
    void shouldRejectRequestedCapacityGreaterThanRoomCapacity() {
        RoomCapacityException exception = assertThrows(RoomCapacityException.class,
                () -> bookingService.createBooking(createRequest(room.id(), start, end, 20)));

        assertTrue(exception.getMessage().contains("Requested capacity 20 exceeds room capacity 10"));
    }

    @Test
    void shouldUpdateBookingWithoutConflict() {
        BookingResponse booking = bookingService.createBooking(createRequest(room.id(), start, end));

        UpdateBookingRequest update = new UpdateBookingRequest("Acme", room.id(), start.plusHours(1), end.plusHours(1), "Rescheduled",10);
        BookingResponse updated = bookingService.updateBooking(booking.id(), update);

        assertEquals(update.startTime(), updated.startTime());
        assertEquals(update.endTime(), updated.endTime());
        assertEquals("Rescheduled", updated.meetingTitle());
    }

    @Test
    void shouldCancelBookingAndFreeSlot() {
        BookingResponse booking = bookingService.createBooking(createRequest(room.id(), start, end));
        assertNotNull(booking);

        bookingService.cancelBooking(booking.id());

        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.searchBookings(
                        booking.id(),
                        null,
                        null,
                        null
                )
        );
        BookingResponse second =
                bookingService.createBooking(
                        createRequest(room.id(), start, end)
                );

        assertNotNull(second);
    }

    @Test
    void shouldReturnEmptyWhenRoomFacilityFilterIsInvalid() {
        assertTrue(roomService.listRooms(null, "INVALID").isEmpty());
    }

    @Test
    void shouldSearchBookingsByBookingIdAndFilters() {
        RoomResponse room = createRoom();
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(9, 0));
        LocalDateTime end = start.plusHours(1);
        BookingResponse booking = bookingService.createBooking(createRequest(room.id(), start, end));

        List<BookingResponse> result = bookingService.searchBookings(booking.id(), room.id(), "Acme", start.toLocalDate());

        assertEquals(1, result.size());
        assertEquals(booking.id(), result.getFirst().id());
    }

    @Test
    void shouldReturnEmptyWhenBookingIdNotFound() {
        int bookingId = 999;
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.searchBookings(bookingId, null, null, null)
        );
    }

    @Test
    void shouldThrowRoomNotFoundWhenSearchingByInvalidRoomId() {
        assertThrows(RoomNotFoundException.class,
                () -> bookingService.searchBookings(null, 999, null, null));
    }

    @Test
    void shouldReturnEmptyWhenSearchingByCompanyNameWithoutBookings() {
        assertTrue(bookingService.searchBookings(null, null, "UnknownCorp", null).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenSearchingByDateWithNoBookings() {
        assertTrue(bookingService.searchBookings(null, null, null, LocalDate.now().plusYears(1)).isEmpty());
    }

    @Test
    void shouldSearchBookingsByRoomCompanyDate() {
        RoomResponse room = createRoom();
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(9, 0));
        LocalDateTime end = start.plusHours(1);
        bookingService.createBooking(createRequest(room.id(), start, end));

        List<BookingResponse> result = bookingService.searchBookings(null, room.id(), "Acme", start.toLocalDate());

        assertEquals(1, result.size());
        assertEquals(room.id(), result.getFirst().roomId());
    }

    @Test
    void shouldAllowOnlyOneOverlappingBookingInConcurrentRequests() throws InterruptedException {
        RoomResponse room = createRoom();
        LocalDateTime start = LocalDateTime.of(LocalDate.now().plusDays(3), LocalTime.of(10, 0));
        LocalDateTime end = start.plusHours(1);
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    bookingService.createBooking(createRequest(room.id(), start, end));
                    successCount.incrementAndGet();
                } catch (RuntimeException ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdownNow();

        assertEquals(1, successCount.get());
    }
    @Test
    void shouldRejectUpdateForUnknownBooking() {
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.updateBooking(
                        999,
                        new UpdateBookingRequest(
                                "Acme",
                                room.id(),
                                start,
                                end,
                                "Meeting",
                                8
                        )
                )
        );
    }
    @Test
    void shouldRejectCancelForUnknownBooking() {
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.cancelBooking(999));
    }
}
