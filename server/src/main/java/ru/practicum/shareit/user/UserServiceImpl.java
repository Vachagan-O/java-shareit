package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findAll() {
        List<UserDto> users = userMapper.toUserDto(userRepository.findAll());
        log.info("Users quantity is: {}", users.size());
        return users;
    }

    @Override
    public UserDto create(User user) {
        user = userRepository.save(user);
        log.info("User is added: {}", user);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = userMapper.toUser(findUserById(id));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);
        log.info("User was updated in DB. New user is: {}", user);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with such id wasn't found"));
        log.info("User was found in DB: {}", user);
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("User with id{} was deleted", id);
    }
}
