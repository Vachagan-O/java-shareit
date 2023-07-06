package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    private final EntityManager em;
    private final UserService service;
    UserMapper userMapper = new UserMapper();

    @Test
    void findAll() {
        List<UserDto> sourceUsers = List.of(
                makeUserDto("Ivan", "ivan@email"),
                makeUserDto("Petr", "petr@email"),
                makeUserDto("Vasilii", "vasilii@email")
        );

        for (UserDto user : sourceUsers) {
            User entity = userMapper.toUser(user);
            em.persist(entity);
        }
        em.flush();

        List<UserDto> targetUsers = service.findAll();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void create() {
        User user = new User(null, "name", "email@mail.ru");

        service.create(user);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User savedUser = query.setParameter("email", user.getEmail())
                .getSingleResult();

        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getName(), equalTo(user.getName()));
        assertThat(savedUser.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void update() {
        User entity = new User(null, "Ivan", "ivan@email");
        em.persist(entity);
        em.flush();

        TypedQuery<Long> queryForId = em.createQuery("Select u.id from User u where u.email = :email", Long.class);
        Long entityId = queryForId.setParameter("email", entity.getEmail())
                .getSingleResult();

        UserDto newUser = makeUserDto("New", "new@email");
        service.update(entityId, newUser);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User updatedUser = query.setParameter("email", entity.getEmail())
                .getSingleResult();

        assertThat(updatedUser.getId(), equalTo(entityId));
        assertThat(updatedUser.getName(), equalTo(newUser.getName()));
        assertThat(updatedUser.getEmail(), equalTo(newUser.getEmail()));
    }

    @Test
    void findUserById() {
        User entity = new User(null, "Ivan", "ivan@email");
        em.persist(entity);
        em.flush();

        TypedQuery<Long> queryForId = em.createQuery("Select u.id from User u where u.email = :email", Long.class);
        Long entityId = queryForId.setParameter("email", entity.getEmail())
                .getSingleResult();

        UserDto targetUser = service.findUserById(entityId);

        assertThat(targetUser.getId(), equalTo(entityId));
        assertThat(targetUser.getName(), equalTo(entity.getName()));
        assertThat(targetUser.getEmail(), equalTo(entity.getEmail()));
    }

    @Test
    void deleteUser() {
        User entity = new User(null, "Ivan", "ivan@email");
        em.persist(entity);
        em.flush();

        TypedQuery<Long> queryForId = em.createQuery("Select u.id from User u where u.email = :email", Long.class);
        Long entityId = queryForId.setParameter("email", entity.getEmail())
                .getSingleResult();

        service.deleteUser(entityId);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class)
                .setParameter("email", entity.getEmail());

        assertThrows(NoResultException.class, query::getSingleResult);
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
