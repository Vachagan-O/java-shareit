package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.dtoToUser(userDto);

        if (isUniqueEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с такой почтой уже существует");
        }
        return UserMapper.userToDto(userRepository.createUser(user));
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User user = UserMapper.dtoToUser(searchUserById(userId));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (user.getEmail().equals(userDto.getEmail())) {
                user.setEmail(userDto.getEmail());
            }
            if (!user.getEmail().equals(userDto.getEmail())) {
                if (isUniqueEmail(userDto.getEmail())) {
                    throw new RuntimeException("Пользователь с такой почтой уже существует");
                }
                //Возможно нужно удалить
                user.setEmail(userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
        log.info("Пользователь {} обновлен.", user.getName());
        return UserMapper.userToDto(userRepository.update(user));
    }

    @Override
    public UserDto searchUserById(Long userId) {
        List<UserDto> users = getAllUsers();
        UserDto userDtoForReturn = null;
        for (UserDto userDto : users) {
            if (userDto.getId().equals(userId)) {
                userDtoForReturn = userDto;
            }
        }
        if (userDtoForReturn == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return userDtoForReturn;
    }

    @Override
    public List<UserDto> getAllUsers() {
        Map<Long, User> userMap = userRepository.getAllUsers();

        List<UserDto> usersDto = new ArrayList<>();

        for (User user : userMap.values()) {
            usersDto.add(UserMapper.userToDto(user));
        }
        return usersDto;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteUser(id);
    }

    @Override
    public boolean isUniqueEmail(String email) {
        List<UserDto> users = new ArrayList<>(getAllUsers());

        for (UserDto user : users) {
            if (user.getEmail().equals(email)) {
                log.info("Этот Email не уникален! '{}'", email);
                return true;
            }
        }
        log.info("Email уникален! '{}'", email);

        return false;
    }
}
