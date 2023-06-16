package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, id, itemDto);
    }

    @GetMapping("/{id}")
    public ItemWithDateAndCommentsDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long id) {
        return itemService.getItemById(userId, id);
    }

    @GetMapping
    public List<ItemWithDateAndCommentsDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(value = "text", required = false) String query) {
        return itemService.getItemsByQuery(userId, query);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}