package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {
    private final EntityManager em;
    private final ItemRequestService service;

    User user;
    User requestor;


    @BeforeEach
    void setUserAndRequestor() {
        User userEntity = User.builder()
                .name("Ivan")
                .email("ivan@email")
                .build();
        em.persist(userEntity);
        em.flush();

        TypedQuery<User> queryForUser = em.createQuery("Select u from User u where u.email = :email", User.class);
        user = queryForUser.setParameter("email", userEntity.getEmail())
                .getSingleResult();

        User requestorEntity = User.builder()
                .name("Requestor")
                .email("requestor@email")
                .build();
        em.persist(requestorEntity);
        em.flush();

        TypedQuery<User> queryForOwner = em.createQuery("Select u from User u where u.email = :email", User.class);
        requestor = queryForOwner.setParameter("email", requestorEntity.getEmail())
                .getSingleResult();
    }

    @Test
    void createItemRequest() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .requestor(requestor)
                .build();

        service.createItemRequest(requestor.getId(), itemRequestDto);

        TypedQuery<ItemRequest> query = em.createQuery(
                "Select ir from ItemRequest ir where ir.description = :description", ItemRequest.class);
        ItemRequest savedItemRequest = query.setParameter("description", itemRequestDto.getDescription())
                .getSingleResult();

        assertThat(savedItemRequest.getId(), notNullValue());
        assertThat(savedItemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(savedItemRequest.getRequestor(), equalTo(requestor));
        assertThat(savedItemRequest.getCreated(), notNullValue());
    }

    @Test
    void getItemRequests() {
        List<ItemRequest> sourceItemRequests = List.of(
                ItemRequest.builder()
                        .description("description1")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequest.builder()
                        .description("description2")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequest.builder()
                        .description("description3")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build()
        );

        for (ItemRequest itemRequest : sourceItemRequests) {
            em.persist(itemRequest);
        }
        em.flush();

        List<ItemRequestDto> targetItemRequests = service.getItemRequests(requestor.getId());

        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequest itemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(itemRequest.getDescription())),
                    hasProperty("requestor", equalTo(itemRequest.getRequestor()))
            )));
        }
    }

    @Test
    void getAllItemRequests() {
        List<ItemRequest> sourceItemRequests = List.of(
                ItemRequest.builder()
                        .description("description1")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequest.builder()
                        .description("description2")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build(),
                ItemRequest.builder()
                        .description("description3")
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        .build()
        );

        for (ItemRequest itemRequest : sourceItemRequests) {
            em.persist(itemRequest);
        }
        em.flush();

        List<ItemRequestDto> targetItemRequests = service.getAllItemRequests(user.getId(), 0, 10);

        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (ItemRequest itemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(itemRequest.getDescription())),
                    hasProperty("requestor", equalTo(itemRequest.getRequestor()))
            )));
        }
    }

    @Test
    void getItemRequestById() {
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description1")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        em.persist(itemRequest);
        em.flush();

        TypedQuery<Long> query = em.createQuery(
                "Select ir.id from ItemRequest ir where ir.description = :description", Long.class);
        Long requestId = query.setParameter("description", itemRequest.getDescription())
                .getSingleResult();

        ItemRequestDto savedItemRequest = service.getItemRequestById(user.getId(), requestId);

        assertThat(savedItemRequest.getId(), notNullValue());
        assertThat(savedItemRequest.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(savedItemRequest.getRequestor(), equalTo(requestor));
        assertThat(savedItemRequest.getCreated(), notNullValue());
    }
}