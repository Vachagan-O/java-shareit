package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Booking createBooking(Long userId, BookingDto bookingDto) {
        User user = userMapper.toUser(userService.findUserById(userId));
        if (bookingDto.getStart().compareTo(bookingDto.getEnd()) >= 0) {
            throw new BadRequestException("Attempt to book with incorrect date");
        }
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item with such id wasn't found"));
        if (!item.getAvailable()) {
            throw new BadRequestException("Attempt to book item which is unavailable");
        }
        if (item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Attempt to book item by user who is its owner");
        }

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking was added: {}", booking);
        return booking;
    }

    @Override
    @Transactional
    public Booking approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = findBookingById(userId, bookingId);

        if (!booking.getItem().getOwnerId().equals(userId)) {
            throw new NotFoundException("Attempt to approve booking by user who isn't item's owner");
        }
        if (Boolean.TRUE.equals(approved)) {
            if (booking.getStatus().equals(BookingStatus.APPROVED)) {
                throw new BadRequestException("Booking status has already been approved");
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(booking);
        log.info("Booking was updated in DB. New booking is: {}", booking);
        return booking;
    }

    @Override
    public Booking findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with such id wasn't found"));
        if (!booking.getItem().getOwnerId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Attempt to get booking by user who is neither item's owner nor booker");
        }
        return booking;
    }

    @Override
    public List<Booking> findBookingsByState(Long userId, String state, Pageable pageRequest) {
        BookingState bookingState;
        userService.findUserById(userId);
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        Iterable<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndCurrentState(userId, LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusIs(userId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusIs(userId, BookingStatus.REJECTED, pageRequest);
        }
        List<Booking> bookingList = new ArrayList<>();
        bookings.forEach(bookingList::add);
        log.info("{} bookings were found for \"{}\" state", bookingList.size(), bookingState);
        return bookingList;
    }

    @Override
    public List<Booking> findBookingByStateForOwner(Long userId, String state, Pageable pageRequest) {
        BookingState bookingState;
        userService.findUserById(userId);
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }

        Iterable<Booking> bookings = new ArrayList<>();
        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByOwnerId(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByOwnerIdCurrentState(userId, LocalDateTime.now(), pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByOwnerIdPastState(userId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByOwnerIdFutureState(userId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageRequest);
        }
        List<Booking> bookingList = new ArrayList<>();
        bookings.forEach(bookingList::add);
        log.info("{} bookings were found for \"{}\" state", bookingList.size(), bookingState);
        return bookingList;
    }
}
