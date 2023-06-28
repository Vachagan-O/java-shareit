package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.dtoToUser(userDto);
        userRepository.save(user);
        return userMapper.userToDto(user);
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User user = userMapper.dtoToUser(searchUserById(userId));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);
        log.info("Пользователь {} обновлен.", user.getName());
        return userMapper.userToDto(user);
    }

    @Override
    public UserDto searchUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Предмет {} найден: {}", user.getName(), user);
        return userMapper.userToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> users = userMapper.userToDtoList(userRepository.findAll());
        log.info("Всего пользователей {}", users.size());
        return users;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
