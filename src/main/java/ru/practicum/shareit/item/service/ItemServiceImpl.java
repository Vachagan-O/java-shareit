package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Repository
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = UserMapper.dtoToUser(userService.searchUserById(userId));
        Item item = ItemMapper.dtoToItem(itemDto);
        return ItemMapper.itemToDto(itemRepository.createItem(user, item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item dataToUpdate = ItemMapper.dtoToItem(itemDto);
        Item item = ItemMapper.dtoToItem(getItemById(userId, itemId));

        if (!userId.equals(item.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "У Вас нет прав на редактирование");
        }
        if (dataToUpdate.getName() != null) {
            item.setName(dataToUpdate.getName());
        }
        if (dataToUpdate.getDescription() != null) {
            item.setDescription(dataToUpdate.getDescription());
        }
        if (dataToUpdate.getAvailable() != null) {
            item.setAvailable(dataToUpdate.getAvailable());
        }
        if (dataToUpdate.getRequest() != null) {
            item.setRequest(dataToUpdate.getRequest());
        }
        log.info("Предмет {} обновлен.", itemDto.getName());

        return ItemMapper.itemToDto(itemRepository.updateItem(item.getId(), item));
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        userService.searchUserById(userId);
        Map<Long, Item> items = getAllItems();
        List<ItemDto> itemList = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().getId().equals(userId)) {
                itemList.add(ItemMapper.itemToDto(item));
            }
        }
        return itemList;
    }

    @Override
    public Map<Long, Item> getAllItems() {
        return itemRepository.getAllItems();
    }

    @Override
    public ItemDto getItemById(Long userId, Long id) {
        Map<Long, Item> items = getAllItems();
        return ItemMapper.itemToDto(items.get(id));
    }

    @Override
    public List<ItemDto> findAllByText(String text, Long userId) {
        List<ItemDto> founded = new ArrayList<>();
        if (text.isBlank()) {
            return founded;
        }
        for (Item item : getAllItems().values()) {
            if (isFounded(text, item)) {
                founded.add(ItemMapper.itemToDto(item));
            }
        }
        return founded;
    }

    private Boolean isFounded(String text, Item item) {
        return (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                item.getDescription().toLowerCase().contains(text.toLowerCase())) && item.getAvailable();
    }
}
