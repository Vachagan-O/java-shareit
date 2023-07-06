package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long id, ItemDto itemDto);

    ItemWithDateAndCommentsDto findItemById(Long userId, Long id);

    List<ItemWithDateAndCommentsDto> findItemsByUserId(Long userId, Integer from, Integer size);

    List<ItemDto> getItemsByQuery(Long userId, String query, Integer from, Integer size);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
