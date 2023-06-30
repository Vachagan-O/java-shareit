package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest createItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}
