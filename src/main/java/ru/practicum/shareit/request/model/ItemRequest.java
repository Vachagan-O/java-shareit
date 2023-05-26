package ru.practicum.shareit.request.model;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

// класс, отвечающий за запрос вещи

@Data
public class ItemRequest {
    private Long id; //идентификатор запроса;
    private String description; //текст запроса, содержащий описание требуемой вещи;
    private User requestor; //пользователь, создавший запрос
    private LocalDateTime created; //дата и время создания запроса
}
