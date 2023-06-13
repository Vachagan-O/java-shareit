package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

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
        userService.searchUserById(userId);
        Item item = itemMapper.dtoToItem(itemDto, userId, null);
        item = itemRepository.save(item);
        log.info("Добавлен предмет: {}", item);
        return item;
        }

    @Override
    public ItemDto updateItem(Long userId, Long id, ItemDto itemDto) {
        Item item = itemRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
        if(!item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем этого предмета");
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
        log.info("Предмет {} обновлен.", item.getName());
        return itemMapper.itemToDto(item);
    }

    @Override
    public List<ItemWithDateAndCommentsDto> getAllItemsByUserId(Long userId) {
        userService.searchUserById(userId);
        List<Item> userItems = itemRepository.findByOwnerId(userId);
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
        log.info("Найдено {} предметов", userItems.size());
        return itemWithDateAndCommentsDtos;
    }

    @Override
    public ItemWithDateAndCommentsDto getItemById(Long userId, Long id) {
        userService.searchUserById(userId);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
        log.info("Предмет найден: {}", item);
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
    public List<ItemDto> getItemsByQuery(Long userId, String query) {
        userService.searchUserById(userId);
        List<ItemDto> userItems = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return userItems;
        }
        userItems = itemMapper.itemToDtoList(itemRepository.search(query));
        return userItems;
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userMapper.dtoToUser(userService.searchUserById(userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
        boolean isAuthorBooker = bookingRepository
                .findByBookerIdAndEndBefore(userId, LocalDateTime.now(), null)
                .stream()
                .map(Booking::getItem)
                .map(Item::getId)
                .anyMatch(x -> x.equals(itemId));
        if (!isAuthorBooker) {
            throw new BadRequestException("Пользователь еще не забронировал данный предмет");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);
        log.info("Комментарий добавлен: {}", comment);
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
