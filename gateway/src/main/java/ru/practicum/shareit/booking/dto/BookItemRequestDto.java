package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.booking.validation.StartBeforeEndDateValid;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@StartBeforeEndDateValid
public class BookItemRequestDto {
    private long itemId;

    @FutureOrPresent
    private LocalDateTime start;

    private LocalDateTime end;
}
