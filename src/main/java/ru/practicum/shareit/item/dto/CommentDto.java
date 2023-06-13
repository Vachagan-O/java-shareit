package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class CommentDto {
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String text;

    private String authorName;

    private LocalDateTime created;
}
