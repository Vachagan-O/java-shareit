package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService requestService;

    @SneakyThrows
    @Test
    void createRequest() {
        Long userId = 1L;
        ItemRequest itemRequest = new ItemRequest(
                1L,
                "description",
                new User(),
                null
        );
        when(requestService.createItemRequest(any(), any())).thenReturn(itemRequest);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.requestor", is(itemRequest.getRequestor()), User.class))
                .andExpect(jsonPath("$.created").isEmpty());
    }

    @SneakyThrows
    @Test
    void getOwnItemRequests() {
        Long userId = 1L;
        when(requestService.getItemRequests(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(requestService).getItemRequests(userId);
    }

    @SneakyThrows
    @Test
    void getAllItemRequests() {
        Long userId = 1L;
        when(requestService.getAllItemRequests(anyLong(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(requestService).getAllItemRequests(userId, 1, 10);
    }

    @SneakyThrows
    @Test
    void getItemRequestById() {
        Long userId = 1L;
        ItemRequestDto itemRequestDto = new ItemRequestDto(
                1L,
                "description",
                new User(),
                null,
                new ArrayList<>()
        );
        when(requestService.getItemRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/" + itemRequestDto.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestor", is(itemRequestDto.getRequestor()), User.class))
                .andExpect(jsonPath("$.created").isEmpty())
                .andExpect(jsonPath("$.items", is(itemRequestDto.getItems())));
        verify(requestService).getItemRequestById(userId, itemRequestDto.getId());
    }
}