package ru.practicum.shareit.user.dto;


import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "Поле не может быть пустым")
    private String name;

    @Email(message = "Некорректный адрес электронной почты")
    @NotBlank(message = "Поле не может быть пустым")
    private String email;
}
