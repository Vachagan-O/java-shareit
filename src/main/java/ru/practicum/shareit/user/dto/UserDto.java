package ru.practicum.shareit.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class UserDto {
    Long id;
    String name;
    @Email(message = "Некорректный адрес электронной почты")
    @NotBlank(message = "Поле не может быть пустым")
    String email;
}
