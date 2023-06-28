package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может состоять только из пробелов")
    private String name;

    @NotEmpty(message = "Описание не может быть пустым")
    private String description;

    @NotNull(message = "Статус не может быть без значения")
    private Boolean available;

    private Long requestId;
}
