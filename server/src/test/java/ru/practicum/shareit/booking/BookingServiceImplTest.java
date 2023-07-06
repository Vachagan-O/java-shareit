package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private Item item;
    private User user;
    private BookingDto dto;
    private Booking booking;

    @BeforeEach
    void setBooking() {
        user = new User();
        user.setId(0L);

        item = new Item();
        item.setId(0L);
        item.setOwnerId(1L);
        item.setAvailable(true);

        dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now());
        dto.setEnd(LocalDateTime.now().plusMinutes(10L));

        booking = new Booking();
        booking.setId(0L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusMinutes(10L));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBooking_whenValidData_thenSavedBooking() {
        Booking bookingToSave = makeBooking(null, dto.getStart(), dto.getEnd(),
                item, user, BookingStatus.WAITING);

        when(userMapper.toUser(any())).thenReturn(user);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Booking actualBooking = bookingService.createBooking(user.getId(), dto);

        assertEquals(bookingToSave, actualBooking);
        verify(bookingRepository).save(any());
    }

    @Test
    void createBooking_whenUserDoesNotExist_thenNotFoundExceptionThrown() {
        doThrow(NotFoundException.class).when(userService).findUserById(user.getId());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(user.getId(), dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenItemDoesNotExist_thenNotFoundExceptionThrown() {
        doThrow(NotFoundException.class).when(itemRepository).findById(dto.getItemId());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(user.getId(), dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenEndLessThanStart_thenBadRequestExceptionThrown() {
        dto.setEnd(dto.getEnd().minusMinutes(20L));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(user.getId(), dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenItemIsNotAvailable_thenBadRequestExceptionThrown() {
        item.setAvailable(false);
        when(itemRepository.findById(dto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(user.getId(), dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenUserIsOwner_thenNotFoundExceptionThrown() {
        item.setOwnerId(user.getId());
        when(itemRepository.findById(dto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(user.getId(), dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenUserApproved_thenSavedBookingWithApprovedStatus() {
        item.setOwnerId(user.getId());
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Booking actualBooking = bookingService.approveBooking(user.getId(),
                booking.getId(), true);

        assertEquals(booking.getId(), actualBooking.getId());
        assertEquals(booking.getStart(), actualBooking.getStart());
        assertEquals(booking.getEnd(), actualBooking.getEnd());
        assertEquals(booking.getItem(), actualBooking.getItem());
        assertEquals(booking.getBooker(), actualBooking.getBooker());
        assertEquals(BookingStatus.APPROVED, actualBooking.getStatus());
        verify(bookingRepository).save(any());
    }

    @Test
    void approveBooking_whenUserNotApproved_thenSavedBookingWithRejectedStatus() {
        item.setOwnerId(user.getId());
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        Booking actualBooking = bookingService.approveBooking(user.getId(),
                booking.getId(), false);

        assertEquals(booking.getId(), actualBooking.getId());
        assertEquals(booking.getStart(), actualBooking.getStart());
        assertEquals(booking.getEnd(), actualBooking.getEnd());
        assertEquals(booking.getItem(), actualBooking.getItem());
        assertEquals(booking.getBooker(), actualBooking.getBooker());
        assertEquals(BookingStatus.REJECTED, actualBooking.getStatus());
        verify(bookingRepository).save(any());
    }

    @Test
    void approveBooking_whenUserIsNotOwner_thenNotFoundExceptionThrown() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService
                .approveBooking(user.getId(), booking.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenBookingStatusIsApproved_thenBadRequestExceptionThrown() {
        item.setOwnerId(user.getId());
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> bookingService
                .approveBooking(user.getId(), booking.getId(), true));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void findBookingById_whenBookingFound_thenReturnedBooking() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.findBookingById(user.getId(), booking.getId());

        assertEquals(booking, actualBooking);
    }

    @Test
    void findBookingById_whenBookingNotFound_thenNotFoundExceptionThrown() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> bookingService
                .findBookingById(0L, 0L));
        assertEquals("Booking with such id wasn't found", notFoundException.getMessage());
    }

    @Test
    void findBookingById_whenUserIsNeitherOwnerNorBooker_thenNotFoundExceptionThrown() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> bookingService
                .findBookingById(2L, booking.getId()));
        assertEquals("Attempt to get booking by user who is neither item's owner nor booker",
                notFoundException.getMessage());
    }

    @Test
    void findBookingsByState_whenStateIsAll_thenAllBranchIsSelected() {
        when(bookingRepository.findByBookerId(anyLong(), any())).thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "all", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerId(anyLong(), any());
    }

    @Test
    void findBookingsByState_whenStateIsCurrent_thenCurrentBranchIsSelected() {
        when(bookingRepository.findByBookerIdAndCurrentState(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "current", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndCurrentState(anyLong(), any(), any());
    }

    @Test
    void findBookingsByState_whenStateIsPast_thenPastBranchIsSelected() {
        when(bookingRepository.findByBookerIdAndEndBefore(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "past", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndEndBefore(anyLong(), any(), any());
    }

    @Test
    void findBookingsByState_whenStateIsFuture_thenFutureBranchIsSelected() {
        when(bookingRepository.findByBookerIdAndStartAfter(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "future", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndStartAfter(anyLong(), any(), any());
    }

    @Test
    void findBookingsByState_whenStateIsWaiting_thenWaitingBranchIsSelected() {
        when(bookingRepository.findByBookerIdAndStatusIs(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "waiting", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndStatusIs(anyLong(), any(), any());
    }

    @Test
    void findBookingsByState_whenStateIsRejected_thenRejectedBranchIsSelected() {
        when(bookingRepository.findByBookerIdAndStatusIs(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService.findBookingsByState(0L, "rejected", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndStatusIs(anyLong(), any(), any());
    }

    @Test
    void findBookingsByState_whenStateIsUnknown_thenBadRequestExceptionThrown() {
        BadRequestException badRequest = assertThrows(BadRequestException.class, () -> bookingService
                .findBookingsByState(0L, "unknown", Pageable.unpaged()));
        assertEquals("Unknown state: unknown", badRequest.getMessage());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsAll_thenAllBranchIsSelected() {
        when(bookingRepository.findByOwnerId(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "all", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerId(any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsCurrent_thenCurrentBranchIsSelected() {
        when(bookingRepository.findByOwnerIdCurrentState(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "Current", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerIdCurrentState(any(), any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsPast_thenPastBranchIsSelected() {
        when(bookingRepository.findByOwnerIdPastState(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "Past", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerIdPastState(any(), any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsFuture_thenFutureBranchIsSelected() {
        when(bookingRepository.findByOwnerIdFutureState(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "Future", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerIdFutureState(any(), any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsWaiting_thenWaitingBranchIsSelected() {
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "waiting", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerIdAndStatus(any(), any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsRejected_thenRejectedBranchIsSelected() {
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Booking> actualBookings = bookingService
                .findBookingByStateForOwner(0L, "Rejected", Pageable.unpaged());

        assertNotNull(actualBookings);
        assertTrue(actualBookings.isEmpty());
        verify(bookingRepository).findByOwnerIdAndStatus(any(), any(), any());
    }

    @Test
    void findBookingByStateForOwner_whenStateIsUnknown_thenBadRequestExceptionThrown() {
        BadRequestException badRequest = assertThrows(BadRequestException.class, () -> bookingService
                .findBookingByStateForOwner(0L, "unknown", Pageable.unpaged()));
        assertEquals("Unknown state: unknown", badRequest.getMessage());
    }

    private Booking makeBooking(Long id, LocalDateTime start,
                                LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}