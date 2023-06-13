package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserRepository extends JpaRepository<User, Long> {
//
//    User createUser(User user);
//
//    User update(User userDto);
//
//    Map<Long, User> getAllUsers();
//
//    void deleteUser(Long id);
}
