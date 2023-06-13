//package ru.practicum.shareit.item.repository;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Repository;
//import ru.practicum.shareit.item.model.Item;
//import ru.practicum.shareit.user.model.User;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Repository
//@Slf4j
//public class ItemRepositoryImpl implements ItemRepository {
//
//    private final Map<Long, Item> items = new HashMap<>();
//    private Long itemId = 1L;
//
//    @Override
//    public Item createItem(User user, Item item) {
//        Long id = itemId++;
//        item.setId(id);
//        item.setOwner(user);
//        items.put(item.getId(), item);
//        return item;
//    }
//
//    @Override
//    public Item updateItem(Long id, Item item) {
//        items.put(id, item);
//        return item;
//    }
//
//    public Map<Long, Item> getAllItems() {
//        return items;
//    }
//}
