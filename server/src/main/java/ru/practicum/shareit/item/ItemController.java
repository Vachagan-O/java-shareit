package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, id, itemDto);
    }

    @GetMapping("/{id}")
    public ItemWithDateAndCommentsDto findItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable Long id) {
        return itemService.findItemById(userId, id);
    }

    @GetMapping
    public List<ItemWithDateAndCommentsDto> findItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                              @RequestParam(value = "from", defaultValue = "0",
                                                                      required = false) Integer from,
                                                              @RequestParam(value = "size", defaultValue = "20",
                                                                      required = false) Integer size) {
        return itemService.findItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(value = "text", required = false) String query,
                                         @RequestParam(value = "from", defaultValue = "0",
                                                 required = false) Integer from,
                                         @RequestParam(value = "size", defaultValue = "20",
                                                 required = false) Integer size) {
        return itemService.getItemsByQuery(userId, query, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId,
                                    @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}
