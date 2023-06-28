package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BookingMapperTest {

    public static final long id = 0L;
    public static final LocalDateTime date = LocalDateTime.now();

    private Booking booking;

    @BeforeEach
    public void beforeEach() {
        User user = new User(id, "name", "user@emali.com");
        Item item = new Item(
                id,
                "name",
                "description",
                true,
                id + 1,
                id + 1);

        booking = new Booking(id,
                date,
                date.plusDays(7),
                item,
                user,
                BookingStatus.APPROVED);
    }

    @Test
    public void toDtoTest() {
        BookingDto result = BookingMapper.bookingToDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getItem().getId(), result.getItemId());
        assertEquals(booking.getBooker().getId(), result.getBookerId());
    }
}