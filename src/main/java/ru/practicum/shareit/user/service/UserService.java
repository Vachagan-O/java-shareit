package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(User user);

    UserDto update(UserDto userDto, Long userId);

    UserDto searchUserById(Long userId);

    List<UserDto> getAllUsers();

    void deleteUser(Long id);
}
