package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    public ItemRequest createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userMapper.toUser(userService.findUserById(userId));
        ItemRequest itemRequest = ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .requestor(user)
                .build();
        itemRequest = requestRepository.save(itemRequest);
        log.info("Request is added: {}", itemRequest);
        return itemRequest;
    }

    @Override
    public List<ItemRequestDto> getItemRequests(Long userId) {
        userService.findUserById(userId);
        List<ItemRequestDto> itemRequests = requestRepository
                .findByRequestorIdOrderByCreated(userId)
                .stream()
                .map(itemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        itemRequests.forEach(ir -> ir.setItems(itemMapper.mapToItemDto(itemRepository.findByRequestId(ir.getId()))));
        log.info("User with id {} has {} requests", userId, itemRequests.size());
        return itemRequests;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size) {
        userService.findUserById(userId);
        Pageable pageRequest = PageRequest.of(from / size, size, Sort.by("created").ascending());
        List<ItemRequestDto> requests = requestRepository.findAll(pageRequest).stream()
                .filter(r -> !r.getRequestor().getId().equals(userId))
                .map(itemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        requests.forEach(ir -> ir.setItems(itemMapper.mapToItemDto(itemRepository.findByRequestId(ir.getId()))));
        log.info("Requests quantity is: {}", requests.size());
        return requests;
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        userService.findUserById(userId);
        ItemRequestDto requestDto = itemRequestMapper.toItemRequestDto(requestRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with such id wasn't found")));

        requestDto.setItems(itemMapper.mapToItemDto(itemRepository.findByRequestId(requestId)));
        log.info("Request was found in DB: {}", requestDto);
        return requestDto;
    }
}
