package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto dto;

    @BeforeEach
    void setUser() {
        user = new User(0L, "name", "email@mail.ru");
        dto = new UserDto(0L, "name", "email@mail.ru");
    }

    @Test
    void findAllWhenUsersFoundThenReturnedUsers() {
        when(userMapper.userToDtoList(anyList())).thenReturn(List.of(dto));

        List<UserDto> actualUsers = userService.getAllUsers();

        assertThat(actualUsers, notNullValue());
        assertThat(actualUsers, hasSize(1));
        assertThat(actualUsers.get(0), equalTo(dto));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createWhenUserSavedThenReturnedUserDto() {
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.dtoToUser(any(UserDto.class))).thenReturn(user);
        when(userMapper.userToDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.create(dto);

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).save(user);
    }

    @Test
    void updateWhenUserUpdatedThenReturnedUserDto() {
        dto = new UserDto(0L, "NewName", "NewEmail");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.dtoToUser(any(UserDto.class))).thenReturn(user);
        when(userMapper.userToDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.update(dto, 0L);

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).save(user);
    }

    @Test
    void findUserByIdWhenUserFoundThenReturnedUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.userToDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.searchUserById(anyLong());

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).findById(anyLong());
    }
}