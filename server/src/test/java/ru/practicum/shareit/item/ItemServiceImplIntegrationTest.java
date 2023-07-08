package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {
    private final EntityManager em;
    private final ItemService service;

    User user;

    @BeforeEach
    void setUser() {
        User entity = new User(null, "Ivan", "ivan@email");
        em.persist(entity);
        em.flush();

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        user = query.setParameter("email", entity.getEmail())
                .getSingleResult();
    }

    @Test
    void createItem() {
        ItemDto itemDto = new ItemDto(null, "Some name",
                "Some description", true, null);

        service.createItem(user.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item savedItem = query.setParameter("name", itemDto.getName())
                .getSingleResult();

        assertThat(savedItem.getId(), notNullValue());
        assertThat(savedItem.getName(), equalTo(itemDto.getName()));
        assertThat(savedItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(savedItem.getOwnerId(), equalTo(user.getId()));
        assertThat(savedItem.getRequestId(), nullValue());
    }

    @Test
    void updateItem() {
        Item item = new Item(null, "name",
                "description", true, user.getId(), null);
        em.persist(item);
        em.flush();

        TypedQuery<Long> queryForId = em.createQuery("Select i.id from Item i where i.name = :name", Long.class);
        Long entityId = queryForId.setParameter("name", item.getName())
                .getSingleResult();

        ItemDto newItem = new ItemDto(null, "Some name",
                "Some description", false, null);
        service.updateItem(item.getOwnerId(), entityId, newItem);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item updatedItem = query.setParameter("name", newItem.getName())
                .getSingleResult();

        assertThat(updatedItem.getId(), notNullValue());
        assertThat(updatedItem.getName(), equalTo(newItem.getName()));
        assertThat(updatedItem.getDescription(), equalTo(newItem.getDescription()));
        assertThat(updatedItem.getAvailable(), equalTo(newItem.getAvailable()));
        assertThat(updatedItem.getOwnerId(), equalTo(item.getOwnerId()));
        assertThat(updatedItem.getRequestId(), nullValue());
    }

    @Test
    void findItemById() {
        Item item = new Item(null, "name",
                "description", true, user.getId(), null);
        em.persist(item);
        em.flush();

        TypedQuery<Long> queryForId = em.createQuery("Select i.id from Item i where i.name = :name", Long.class);
        Long entityId = queryForId.setParameter("name", item.getName())
                .getSingleResult();

        ItemWithDateAndCommentsDto targetItem = service.findItemById(user.getId(), entityId);

        assertThat(targetItem.getId(), notNullValue());
        assertThat(targetItem.getName(), equalTo(item.getName()));
        assertThat(targetItem.getDescription(), equalTo(item.getDescription()));
        assertThat(targetItem.getAvailable(), equalTo(item.getAvailable()));
        assertThat(targetItem.getLastBooking(), nullValue());
        assertThat(targetItem.getNextBooking(), nullValue());
        assertThat(targetItem.getComments(), equalTo(Collections.emptyList()));
    }

    @Test
    void findItemsByUserId() {
        List<Item> sourceItems = List.of(
                new Item(null, "name1", "description", true, user.getId(), null),
                new Item(null, "name2", "description", true, user.getId(), null),
                new Item(null, "name3", "description", true, user.getId(), null)
        );

        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();

        List<ItemWithDateAndCommentsDto> targetItems = service.findItemsByUserId(user.getId(), 0, 10);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (Item item : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable())),
                    hasProperty("lastBooking", nullValue()),
                    hasProperty("nextBooking", nullValue()),
                    hasProperty("comments", equalTo(Collections.emptyList()))
            )));
        }
    }

    @Test
    void getItemsByQuery() {
        List<Item> sourceItems = List.of(
                new Item(null, "name", "description1", true, user.getId(), null),
                new Item(null, "name", "description2", true, user.getId(), null),
                new Item(null, "name", "description3", true, user.getId(), null)
        );

        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();

        List<ItemDto> targetItems = service.getItemsByQuery(user.getId(), "name", 0, 10);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (Item item : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable()))
            )));
        }
    }

    @Test
    void createComment() {
        Item item = new Item(null, "name",
                "description", true, user.getId(), null);
        em.persist(item);
        em.flush();

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item savedItem = query.setParameter("name", item.getName())
                .getSingleResult();

        Booking booking = new Booking(
                null,
                LocalDateTime.of(2023, 1, 20, 0, 0),
                LocalDateTime.of(2023, 1, 30, 0, 0),
                savedItem,
                user,
                BookingStatus.APPROVED);
        em.persist(booking);
        em.flush();

        CommentDto commentToSave = new CommentDto(null, "comment", null, null);

        CommentDto savedComment = service.createComment(user.getId(), savedItem.getId(), commentToSave);

        assertThat(savedComment.getId(), notNullValue());
        assertThat(savedComment.getText(), equalTo(commentToSave.getText()));
        assertThat(savedComment.getAuthorName(), equalTo(user.getName()));
        assertThat(savedComment.getCreated(), notNullValue());
    }
}