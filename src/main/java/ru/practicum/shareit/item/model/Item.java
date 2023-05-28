package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;
    @NotBlank(message = "Название не может состоять только из пробелов")
    private String name;
    @NotEmpty(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Статус не может быть без значения")
    private Boolean available;
    private User owner;
    private ItemRequest request;
}
