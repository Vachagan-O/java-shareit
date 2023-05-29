package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Map;

@Repository
public interface ItemRepository {

    Item createItem(User user, Item item);

    Item updateItem(Long id, Item item);

    Map<Long, Item> getAllItems();
}
