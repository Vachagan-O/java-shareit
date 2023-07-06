package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.Marker;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Validated(Marker.OnCreate.class) UserRequestDto requestDto) {
        log.info("Creating user {}", requestDto);
        return userClient.createUser(requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id,
                                             @RequestBody @Validated(Marker.OnUpdate.class) UserRequestDto requestDto) {
        log.info("Updating user with id={}, new User={}", id, requestDto);
        return userClient.updateUser(id, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Getting users...");
        return userClient.getUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Long id) {
        log.info("Getting user with id={}...", id);
        return userClient.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id={}", id);
        return userClient.deleteUser(id);
    }
}
