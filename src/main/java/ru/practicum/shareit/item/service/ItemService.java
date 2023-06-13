package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {
    Item createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long id, ItemDto itemDto);

    List<ItemWithDateAndCommentsDto> getAllItemsByUserId(Long userId);

    //Map<Long, Item> getAllItems();

    ItemWithDateAndCommentsDto getItemById(Long userId, Long id);

    List<ItemDto> getItemsByQuery(Long userId, String query);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}