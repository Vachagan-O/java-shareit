package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        user = new User(0L, "name", "email");
        dto = new UserDto(0L, "name", "email");
    }

    @Test
    void findAll_whenUsersFound_thenReturnedUsers() {
        when(userMapper.toUserDto(anyList())).thenReturn(List.of(dto));

        List<UserDto> actualUsers = userService.findAll();

        assertThat(actualUsers, notNullValue());
        assertThat(actualUsers, hasSize(1));
        assertThat(actualUsers.get(0), equalTo(dto));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void create_whenUserSaved_thenReturnedUserDto() {
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.create(user);

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).save(user);
    }

    @Test
    void update_whenUserUpdated_thenReturnedUserDto() {
        dto = new UserDto(0L, "NewName", "NewEmail");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        when(userMapper.toUserDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.update(0L, dto);

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).save(user);
    }

    @Test
    void findUserById_whenUserFound_thenReturnedUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(any(User.class))).thenReturn(dto);

        UserDto actualUserDto = userService.findUserById(anyLong());

        assertThat(actualUserDto, equalTo(dto));
        verify(userRepository).findById(anyLong());
    }
}