package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemController.class)
class ItemControllerITest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    @SneakyThrows
    @Test
    void createItem() {
        Long userId = 1L;
        Item item = new Item(1L, "name",
                "description", true, null, null);
        when(itemService.createItem(any(), any())).thenReturn(item);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.ownerId").isEmpty())
                .andExpect(jsonPath("$.requestId").isEmpty());
    }

    @SneakyThrows
    @Test
    void updateItem() {
        Long userId = 1L;
        ItemDto itemDto = new ItemDto(1L, "name",
                "description", true, null);
        when(itemService.updateItem(any(), any(), any())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/" + itemDto.getId())
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId").isEmpty());
    }

    @SneakyThrows
    @Test
    void findItemById() {
        Long userId = 1L;
        ItemWithDateAndCommentsDto itemDto = new ItemWithDateAndCommentsDto(
                1L,
                "name",
                "description",
                true,
                null,
                null,
                null);
        when(itemService.findItemById(any(), any())).thenReturn(itemDto);

        mockMvc.perform(get("/items/" + itemDto.getId())
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty());
        verify(itemService).findItemById(userId, itemDto.getId());
    }

    @SneakyThrows
    @Test
    void findItemsByUserId() {
        Long userId = 1L;
        when(itemService.findItemsByUserId(any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(itemService).findItemsByUserId(userId, 1, 10);
    }

    @SneakyThrows
    @Test
    void getItemsByQuery() {
        Long userId = 1L;
        when(itemService.getItemsByQuery(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "query")
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(itemService).getItemsByQuery(userId, "query", 1, 10);
    }

    @SneakyThrows
    @Test
    void createComment() {
        Long userId = 1L;
        long itemId = 0L;
        CommentDto commentDto = new CommentDto(1L, "text", "name", null);
        when(itemService.createComment(any(), any(), any())).thenReturn(commentDto);

        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created").isEmpty());
    }
}