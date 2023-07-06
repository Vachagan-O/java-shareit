package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();

    UserDto create(User user);

    UserDto update(Long id, UserDto userDto);

    UserDto findUserById(Long id);

    void deleteUser(Long id);
}
