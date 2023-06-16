package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRequestDto {
    private Long id;

    @NotBlank
    private String description;

    private User requestor;

    private Long created;
}
