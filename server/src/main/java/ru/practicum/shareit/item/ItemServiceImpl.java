package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    public Item createItem(Long userId, ItemDto itemDto) {
        userService.findUserById(userId);
        Item item = itemMapper.toItem(itemDto, userId);
        item = itemRepository.save(item);
        log.info("Item is added: {}", item);
        return item;
    }

    @Override
    public ItemDto updateItem(Long userId, Long id, ItemDto itemDto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with such id wasn't found"));

        if (!item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Attempt to update item by user who isn't its owner");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(item);
        log.info("Item was updated in DB. New item is: {}", item);
        return itemMapper.mapToItemDto(item);
    }

    @Override
    public ItemWithDateAndCommentsDto findItemById(Long userId, Long id) {
        userService.findUserById(userId);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item with such id wasn't found"));
        log.info("Item was found in DB: {}", item);
        List<Comment> comment = commentRepository.findByItemId(id);
        BookingDto lastBooking = getLastBooking(item.getId());
        BookingDto nextBooking = getNextBooking(item.getId());

        if (item.getOwnerId().equals(userId)) {
            return ItemWithDateAndCommentsDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .lastBooking(lastBooking)
                    .nextBooking(nextBooking)
                    .comments(commentMapper.toCommentDtos(comment))
                    .build();
        } else {
            return ItemWithDateAndCommentsDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .comments(commentMapper.toCommentDtos(comment))
                    .build();
        }
    }

    @Override
    public List<ItemWithDateAndCommentsDto> findItemsByUserId(Long userId, Integer from, Integer size) {
        userService.findUserById(userId);
        List<Item> userItems = itemRepository.findByOwnerIdOrderById(userId, PageRequest.of(from / size, size));
        List<ItemWithDateAndCommentsDto> itemWithDateAndCommentsDtos = new ArrayList<>();
        List<CommentDto> commentDtos = commentMapper.toCommentDtos(commentRepository.findByOwnerId(userId));
        for (Item item : userItems) {
            BookingDto lastBooking = getLastBooking(item.getId());
            BookingDto nextBooking = getNextBooking(item.getId());

            itemWithDateAndCommentsDtos.add(ItemWithDateAndCommentsDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .lastBooking(lastBooking)
                    .nextBooking(nextBooking)
                    .comments(commentDtos)
                    .build());
        }
        log.info("User with id {} has {} items", userId, itemWithDateAndCommentsDtos.size());
        return itemWithDateAndCommentsDtos;
    }

    @Override
    public List<ItemDto> getItemsByQuery(Long userId, String query, Integer from, Integer size) {
        userService.findUserById(userId);
        List<ItemDto> userItems = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            log.info("Query is empty or null");
            return userItems;
        }
        userItems = itemMapper.mapToItemDto(itemRepository.search(query, PageRequest.of(from / size, size)));
        log.info("{} items were found for \"{}\" query", userItems.size(), query);
        return userItems;
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userMapper.toUser(userService.findUserById(userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with such id wasn't found"));
        boolean isAuthorBooker = bookingRepository
                .findByBookerIdAndEndBefore(userId, LocalDateTime.now(), null)
                .stream()
                .map(Booking::getItem)
                .map(Item::getId)
                .anyMatch(x -> x.equals(itemId));
        if (!isAuthorBooker) {
            throw new BadRequestException("Attempt to post comment by user who hasn't booked this item yet");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);
        log.info("Comment was added: {}", comment);
        return commentMapper.toCommentDto(comment);
    }

    private BookingDto getLastBooking(Long itemId) {
        Booking booking = bookingRepository
                .findByItemIdOrderByStart(itemId)
                .stream()
                .filter(x -> x.getStatus().equals(BookingStatus.APPROVED))
                .filter(x -> x.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);
        return booking != null ? bookingMapper.toBookingDto(booking) : null;
    }

    private BookingDto getNextBooking(Long itemId) {
        Booking booking = bookingRepository
                .findByItemIdOrderByStart(itemId)
                .stream()
                .filter(x -> x.getStatus().equals(BookingStatus.APPROVED))
                .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
        return booking != null ? bookingMapper.toBookingDto(booking) : null;
    }
}
