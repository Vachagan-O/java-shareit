package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;

    private Booking booking;
    Long userId = 1L;

    @BeforeEach
    void setUp() {
        booking = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new Item(),
                new User(),
                BookingStatus.APPROVED
        );
    }

    @SneakyThrows
    @Test
    void createBooking() {
        when(bookingService.createBooking(any(), any())).thenReturn(booking);

        String result = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(booking))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(booking), result);
    }

    @SneakyThrows
    @Test
    void updateBooking() {
        when(bookingService.approveBooking(any(), any(), any())).thenReturn(booking);

        String result = mockMvc.perform(patch("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false")
                        .content(objectMapper.writeValueAsString(booking))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(booking), result);
    }

    @SneakyThrows
    @Test
    void findBookingById() {
        when(bookingService.findBookingById(any(), any())).thenReturn(booking);

        String result = mockMvc.perform(get("/bookings/" + booking.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(booking), result);
    }

    @SneakyThrows
    @Test
    void findBookingsByState() {
        when(bookingService.findBookingsByState(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "future")
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(bookingService).findBookingsByState(userId, "future",
                PageRequest.of(1 / 10, 10, Sort.by("start").descending()));
    }

    @SneakyThrows
    @Test
    void findBookingsByStateForOwner() {
        when(bookingService.findBookingByStateForOwner(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "future")
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(bookingService).findBookingByStateForOwner(userId, "future", PageRequest.of(1 / 10, 10));
    }
}