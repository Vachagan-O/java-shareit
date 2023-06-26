package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //идентификатор запроса;

    private String description; //текст запроса, содержащий описание требуемой вещи;

    @ManyToOne
    @JoinColumn(name = "requestor_id")
    private User requestor; //пользователь, создавший запрос

    @Column(name = "create_date")
    private LocalDateTime created; //дата и время создания запроса
}
