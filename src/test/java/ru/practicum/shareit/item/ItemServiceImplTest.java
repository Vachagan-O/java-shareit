package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setItem() {
        item = Item.builder()
                .id(0L)
                .name("name")
                .description("description")
                .available(true)
                .ownerId(0L)
                .build();
        itemDto = ItemDto.builder()
                .id(0L)
                .name("Some name")
                .description("Some description")
                .available(true)
                .build();
    }

    @Test
    void createItem_whenValidData_thenSavedItem() {
        when(itemRepository.save(any())).thenReturn(item);

        Item actualItem = itemService.createItem(anyLong(), itemDto);

        assertThat(actualItem, equalTo(item));
        verify(itemRepository).save(any());
    }

    @Test
    void updateItem_whenValidData_thenSavedItem() {
        Long userId = 0L;
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(itemMapper.itemToDto(any(Item.class))).thenReturn(itemDto);

        ItemDto actualItemDto = itemService.updateItem(userId, item.getId(), itemDto);

        assertThat(actualItemDto, equalTo(itemDto));
        verify(itemRepository).save(any());
    }

    @Test
    void updateItem_whenItemNotFound_thenNotFoundExceptionThrown() {
        Long userId = 0L;

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> itemService
                .updateItem(userId, anyLong(), itemDto));
        assertEquals("Предмет с таким id не найден",
                notFoundException.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenUserIsNotOwner_thenNotFoundExceptionThrown() {
        Long userId = 1L;
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> itemService
                .updateItem(userId, anyLong(), itemDto));
        assertEquals("Пользователь не является владельцем этого предмета",
                notFoundException.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void findItemById_whenUserIsNotOwner_thenReturnedItem() {
        Long userId = 0L;
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());

        ItemWithDateAndCommentsDto dto = itemService.getItemById(userId, anyLong());

        assertThat(dto.getId(), equalTo(item.getId()));
        assertThat(dto.getName(), equalTo(item.getName()));
        assertThat(dto.getDescription(), equalTo(item.getDescription()));
        assertThat(dto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(dto.getLastBooking(), nullValue());
        assertThat(dto.getNextBooking(), nullValue());
        assertThat(dto.getComments(), equalTo(Collections.emptyList()));
        verify(itemRepository, times(1)).findById(any());
        verify(commentRepository, times(1)).findByItemId(any());
        verify(bookingRepository, times(2)).findByItemIdOrderByStart(any());
    }

    @Test
    void findItemsByUserId_whenUserExists_thenReturnedItems() {
        Long userId = 0L;
        List<Item> expectedItems = List.of(
                new Item(0L, "name", "description", true, 0L, 0L),
                new Item(1L, "name", "description", true, 0L, 0L),
                new Item(2L, "name", "description", true, 0L, 0L)
        );
        when(itemRepository.findByOwnerId(anyLong(), any(PageRequest.class))).thenReturn(expectedItems);

        List<ItemWithDateAndCommentsDto> actualItemDtos = itemService
                .getAllItemsByUserId(userId, anyInt(), 1);

        assertThat(actualItemDtos, hasSize(3));
        for (Item item : expectedItems) {
            assertThat(actualItemDtos,
                    hasItem(hasProperty("id", equalTo(item.getId()))));
        }
        verify(commentRepository, times(1)).findByOwnerId(any());
    }

    @Test
    void getItemsByQuery_whenQueryIsNotEmpty_thenReturnedItemDtos() {
        Long userId = 0L;
        when(itemRepository.search(any(), any(PageRequest.class))).thenReturn(Collections.emptyList());
        when(itemMapper.itemToDtoList(anyList())).thenReturn(List.of(itemDto));

        List<ItemDto> actualItemDtos = itemService
                .getItemsByQuery(userId, "query", anyInt(), 1);

        assertThat(actualItemDtos, hasSize(1));
        verify(itemRepository).search(any(), any(PageRequest.class));
    }

    @Test
    void createComment_whenValidData_thenSavedComment() {
        Long userId = 0L;
        CommentDto dto = new CommentDto(0L, "text", null, null);
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndEndBefore(any(), any(), any()))
                .thenReturn(List.of(
                        new Booking(null, null, null, item, null, null)));

        itemService.createComment(userId, item.getId(), dto);

        verify(commentRepository).save(any());
    }

    @Test
    void createComment_whenUserIsNotAuthor_thenBadRequestExceptionThrown() {
        Long userId = 0L;
        Long itemId = 1L;
        CommentDto dto = new CommentDto(0L, "text", null, null);
        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndEndBefore(any(), any(), any()))
                .thenReturn(List.of(
                        new Booking(null, null, null, item, null, null)));

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> itemService
                .createComment(userId, itemId, dto));
        assertEquals("Пользователь еще не забронировал данный предмет",
                badRequestException.getMessage());
        verify(commentRepository, never()).save(any());

    }

}