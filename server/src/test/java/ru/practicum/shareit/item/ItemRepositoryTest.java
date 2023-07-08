package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void search() {
        itemRepository.save(Item.builder()
                .name("some Item")
                .description("description")
                .available(true)
                .build());

        List<Item> result = itemRepository.search("item", Pageable.unpaged());

        assertThat(result, iterableWithSize(1));
        assertThat("some Item", equalTo(result.get(0).getName()));
    }
}