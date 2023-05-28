package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    @Email(message = "Некорректный адрес электронной почты")
    @NotBlank(message = "Поле не может быть пустым")
    @Size(max = 30, message = "Вы превысили максимальное количество символов")
    private String email;
}


