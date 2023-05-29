package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

@Repository
public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getAllItemsByUserId(Long userId);

    Map<Long, Item> getAllItems();

    ItemDto getItemById(Long userId, Long id);

    List<ItemDto> findAllByText(String text, Long userId);
}