package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam(value = "approved") Boolean approved) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking findBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping()
    public List<Booking> findBookingsByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(value = "state", defaultValue = "ALL",
                                                     required = false) String state) {
        return bookingService.findBookingsByState(userId, state);
    }

    @GetMapping("/owner")
    public List<Booking> findBookingsByStateForOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(value = "state", defaultValue = "ALL",
                                                             required = false) String state) {
        return bookingService.findBookingByStateForOwner(userId, state);
    }
}
