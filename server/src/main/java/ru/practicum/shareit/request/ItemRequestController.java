package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequest createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestBody ItemRequestDto itemRequestDto) {
        return requestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(value = "from", defaultValue = "0",
                                                           required = false) Integer from,
                                                   @RequestParam(value = "size", defaultValue = "20",
                                                           required = false) Integer size) {
        return requestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long requestId) {
        return requestService.getItemRequestById(userId, requestId);
    }
}
