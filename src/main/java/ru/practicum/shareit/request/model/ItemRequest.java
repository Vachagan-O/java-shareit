package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRequest {
    private Long id; //идентификатор запроса;
    private String description; //текст запроса, содержащий описание требуемой вещи;
    private User requestor; //пользователь, создавший запрос
    private LocalDateTime created; //дата и время создания запроса
}
